package com.drivecare.service;

import com.drivecare.dto.BookingMapper;
import com.drivecare.dto.BookingResponse;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.Role;
import com.drivecare.entity.User;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.BookingRepository;
import com.drivecare.repository.MechanicRepository;
import com.drivecare.repository.PaymentRepository;
import com.drivecare.repository.ReviewRepository;
import com.drivecare.repository.UserRepository;
import com.drivecare.repository.VehicleRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final VehicleRepository vehicleRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;

    public DashboardService(
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            VehicleRepository vehicleRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            ReviewRepository reviewRepository) {
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.vehicleRepository = vehicleRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.reviewRepository = reviewRepository;
    }

    public Map<String, Object> adminDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalCustomers", userRepository.countByRole(Role.CUSTOMER));
        data.put("totalMechanics", mechanicRepository.count());
        data.put("totalVehicles", vehicleRepository.count());
        data.put("totalBookings", bookingRepository.count());
        data.put("pendingBookings", bookingRepository.countByStatus(BookingStatus.PENDING));
        data.put("assignedBookings", bookingRepository.countByStatus(BookingStatus.ASSIGNED));
        data.put("inProgressServices", bookingRepository.countByStatus(BookingStatus.IN_PROGRESS));
        data.put("completedServices", bookingRepository.countByStatus(BookingStatus.COMPLETED));
        data.put("revenue", paymentRepository.totalPaidRevenue());
        return data;
    }

    public Map<String, Object> adminReports() {
        int year = Year.now().getValue();
        Map<String, Object> reports = new LinkedHashMap<>();
        reports.put("year", year);
        reports.put("monthlyBookings", toMonthly(bookingRepository.monthlyBookingStats(year)));
        reports.put("monthlyRevenue", toMonthlyRevenue(bookingRepository.monthlyRevenueStats(year)));
        reports.put("popularServices", toNamedCounts(bookingRepository.countBookingsByService()));
        reports.put("mechanicPerformance", mechanicPerformance());
        return reports;
    }

    public Map<String, Object> customerDashboard(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        List<BookingResponse> bookings = bookingRepository
                .findByCustomerIdOrderByBookingDateDescBookingTimeDesc(user.getId())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();

        List<BookingResponse> upcoming = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING || b.getStatus() == BookingStatus.ASSIGNED)
                .toList();
        BookingResponse active = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS)
                .findFirst()
                .orElse(null);
        List<BookingResponse> history = bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED || b.getStatus() == BookingStatus.CANCELLED)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("vehicles", vehicleRepository.findByUserId(user.getId()));
        data.put("upcomingBookings", upcoming);
        data.put("activeService", active);
        data.put("serviceHistory", history);
        data.put("payments", paymentRepository.findByCustomerIdOrderByIdDesc(user.getId()));
        data.put("reviews", reviewRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId()));
        return data;
    }

    public Map<String, Object> mechanicDashboard(String email) {
        var mechanic = mechanicRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Mechanic not found", HttpStatus.NOT_FOUND));

        List<BookingResponse> assigned = bookingRepository
                .findByMechanicIdOrderByBookingDateAscBookingTimeAsc(mechanic.getId())
                .stream()
                .map(BookingMapper::toResponse)
                .toList();

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("todayJobs", bookingRepository.findByMechanicIdAndBookingDate(mechanic.getId(), LocalDate.now())
                .stream().map(BookingMapper::toResponse).toList());
        data.put("assignedJobs", assigned.stream().filter(b -> b.getStatus() == BookingStatus.ASSIGNED).toList());
        data.put("inProgressServices", assigned.stream().filter(b -> b.getStatus() == BookingStatus.IN_PROGRESS).toList());
        data.put("completedServices", assigned.stream().filter(b -> b.getStatus() == BookingStatus.COMPLETED).toList());
        data.put("schedule", assigned.stream()
                .filter(b -> b.getStatus() != BookingStatus.CANCELLED)
                .toList());
        return data;
    }

    private List<Map<String, Object>> toMonthly(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", row[0]);
            item.put("bookings", row[1]);
            item.put("amount", row[2]);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> toMonthlyRevenue(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("month", row[0]);
            item.put("revenue", row[1]);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> toNamedCounts(List<Object[]> rows) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("serviceName", row[0]);
            item.put("bookings", row[1]);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> mechanicPerformance() {
        Map<Long, Double> ratings = new HashMap<>();
        for (Object[] row : reviewRepository.averageRatingByMechanic()) {
            ratings.put((Long) row[0], ((Number) row[1]).doubleValue());
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : bookingRepository.completedJobsByMechanic()) {
            Long mechanicId = (Long) row[0];
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("mechanicId", mechanicId);
            item.put("mechanicName", row[1]);
            item.put("completedJobs", row[2]);
            item.put("averageRating", ratings.getOrDefault(mechanicId, 0.0));
            result.add(item);
        }
        return result;
    }
}
