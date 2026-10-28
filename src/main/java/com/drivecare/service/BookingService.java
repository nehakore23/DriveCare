package com.drivecare.service;

import com.drivecare.dto.BookingMapper;
import com.drivecare.dto.BookingRequest;
import com.drivecare.dto.BookingResponse;
import com.drivecare.dto.MechanicJobUpdateRequest;
import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.Mechanic;
import com.drivecare.entity.User;
import com.drivecare.entity.Vehicle;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.BookingRepository;
import com.drivecare.repository.MechanicRepository;
import com.drivecare.repository.ServiceRepository;
import com.drivecare.repository.UserRepository;
import com.drivecare.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    public BookingService(
            BookingRepository bookingRepository,
            VehicleRepository vehicleRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            NotificationService notificationService,
            PaymentService paymentService) {

        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
    }

    public BookingResponse createBooking(BookingRequest request, String email) {
        User user = findUser(email);
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ApiException("Vehicle not found", HttpStatus.NOT_FOUND));

        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new ApiException("You cannot book another customer's vehicle", HttpStatus.FORBIDDEN);
        }

        com.drivecare.entity.Service service = serviceRepository.findById(request.getServiceId())
                .orElseThrow(() -> new ApiException("Service not found", HttpStatus.NOT_FOUND));

        if (!service.isStatus()) {
            throw new ApiException("This service is currently unavailable");
        }

        Booking booking = new Booking();
        booking.setCustomer(user);
        booking.setVehicle(vehicle);
        booking.setService(service);
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(service.getPrice());
        booking.setNotes(request.getNotes());

        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream().map(BookingMapper::toResponse).toList();
    }

    public List<BookingResponse> getPendingBookings() {
        return bookingRepository.findByStatus(BookingStatus.PENDING).stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public BookingResponse getBookingById(Long id) {
        return BookingMapper.toResponse(findBooking(id));
    }

    public BookingResponse getCustomerBooking(Long id, String email) {
        User user = findUser(email);
        Booking booking = findBooking(id);
        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new ApiException("Booking not found", HttpStatus.NOT_FOUND);
        }
        return BookingMapper.toResponse(booking);
    }

    public List<BookingResponse> getCustomerBookings(String email) {
        User user = findUser(email);
        return bookingRepository.findByCustomerIdOrderByBookingDateDescBookingTimeDesc(user.getId())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public BookingResponse cancelBooking(Long id, String email) {
        User user = findUser(email);
        Booking booking = findBooking(id);
        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new ApiException("You can only cancel your own booking", HttpStatus.FORBIDDEN);
        }
        if (booking.getStatus() == BookingStatus.COMPLETED || booking.getStatus() == BookingStatus.IN_PROGRESS) {
            throw new ApiException("This booking can no longer be cancelled");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public BookingResponse assignMechanic(Long bookingId, Long mechanicId) {
        Booking booking = findBooking(bookingId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ApiException("Cannot assign a cancelled booking");
        }
        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new ApiException("Cannot reassign a completed booking");
        }

        Mechanic mechanic = mechanicRepository.findById(mechanicId)
                .orElseThrow(() -> new ApiException("Mechanic not found", HttpStatus.NOT_FOUND));
        if (!mechanic.isStatus() || !mechanic.isAvailable()) {
            throw new ApiException("Mechanic is not available");
        }

        booking.setMechanic(mechanic);
        booking.setStatus(BookingStatus.ASSIGNED);
        booking.setAssignedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        notificationService.notifyAssignment(saved);
        return BookingMapper.toResponse(saved);
    }

    public List<BookingResponse> getMechanicJobs(String email) {
        Mechanic mechanic = findMechanic(email);
        return bookingRepository.findByMechanicIdOrderByBookingDateAscBookingTimeAsc(mechanic.getId())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public List<BookingResponse> getTodayJobs(String email) {
        Mechanic mechanic = findMechanic(email);
        return bookingRepository.findByMechanicIdAndBookingDate(mechanic.getId(), LocalDate.now())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public List<BookingResponse> getMechanicJobsByStatus(String email, BookingStatus status) {
        Mechanic mechanic = findMechanic(email);
        return bookingRepository.findByMechanicIdAndStatus(mechanic.getId(), status)
                .stream()
                .map(BookingMapper::toResponse)
                .toList();
    }

    public BookingResponse startJob(Long bookingId, String email) {
        Booking booking = ownedJob(bookingId, email);
        if (booking.getStatus() != BookingStatus.ASSIGNED) {
            throw new ApiException("Only assigned jobs can be started");
        }
        booking.setStatus(BookingStatus.IN_PROGRESS);
        booking.setStartedAt(LocalDateTime.now());
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public BookingResponse updateJobNotes(Long bookingId, MechanicJobUpdateRequest request, String email) {
        Booking booking = ownedJob(bookingId, email);
        if (booking.getStatus() != BookingStatus.IN_PROGRESS && booking.getStatus() != BookingStatus.ASSIGNED) {
            throw new ApiException("Notes can be updated only for active jobs");
        }
        if (request.getMechanicNotes() != null) {
            booking.setMechanicNotes(request.getMechanicNotes());
        }
        if (request.getChecklist() != null) {
            booking.setChecklist(request.getChecklist());
        }
        return BookingMapper.toResponse(bookingRepository.save(booking));
    }

    public BookingResponse completeJob(Long bookingId, String email) {
        Booking booking = ownedJob(bookingId, email);
        if (booking.getStatus() != BookingStatus.IN_PROGRESS) {
            throw new ApiException("Only in-progress jobs can be completed");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setCompletedAt(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        paymentService.createPaidReceipt(saved);
        return BookingMapper.toResponse(saved);
    }

    private Booking ownedJob(Long bookingId, String email) {
        Mechanic mechanic = findMechanic(email);
        Booking booking = findBooking(bookingId);
        if (booking.getMechanic() == null || !booking.getMechanic().getId().equals(mechanic.getId())) {
            throw new ApiException("This job is not assigned to you", HttpStatus.FORBIDDEN);
        }
        return booking;
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException("Booking not found", HttpStatus.NOT_FOUND));
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    private Mechanic findMechanic(String email) {
        return mechanicRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Mechanic not found", HttpStatus.NOT_FOUND));
    }
}
