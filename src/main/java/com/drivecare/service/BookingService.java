package com.drivecare.service;

import com.drivecare.dto.BookingRequest;
import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.User;
import com.drivecare.entity.Vehicle;
import com.drivecare.repository.BookingRepository;
import com.drivecare.repository.ServiceRepository;
import com.drivecare.repository.UserRepository;
import com.drivecare.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;

    public BookingService(
            BookingRepository bookingRepository,
            VehicleRepository vehicleRepository,
            ServiceRepository serviceRepository,
            UserRepository userRepository) {

        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.serviceRepository = serviceRepository;
        this.userRepository = userRepository;
    }

    public Booking createBooking(
            BookingRequest request,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        Vehicle vehicle = vehicleRepository
                .findById(request.getVehicleId())
                .orElseThrow(() ->
                        new RuntimeException("Vehicle not found"));

        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new RuntimeException(
                    "You cannot book another customer's vehicle");
        }

        com.drivecare.entity.Service service =
                serviceRepository.findById(request.getServiceId())
                        .orElseThrow(() ->
                                new RuntimeException("Service not found"));

        Booking booking = new Booking();

        booking.setVehicle(vehicle);
        booking.setService(service);
        booking.setBookingDate(request.getBookingDate());
        booking.setBookingTime(request.getBookingTime());
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(service.getPrice());
        booking.setNotes(request.getNotes());

        return bookingRepository.save(booking);
    }

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Booking not found"));
    }
}