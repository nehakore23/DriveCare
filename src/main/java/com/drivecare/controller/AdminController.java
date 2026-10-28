package com.drivecare.controller;

import com.drivecare.dto.AssignMechanicRequest;
import com.drivecare.dto.BookingResponse;
import com.drivecare.dto.MechanicRequest;
import com.drivecare.entity.Mechanic;
import com.drivecare.entity.Payment;
import com.drivecare.entity.Review;
import com.drivecare.entity.Service;
import com.drivecare.entity.User;
import com.drivecare.entity.Vehicle;
import com.drivecare.service.BookingService;
import com.drivecare.service.DashboardService;
import com.drivecare.service.MechanicService;
import com.drivecare.service.PaymentService;
import com.drivecare.service.ReviewService;
import com.drivecare.service.ServiceService;
import com.drivecare.service.UserService;
import com.drivecare.service.VehicleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final DashboardService dashboardService;
    private final UserService userService;
    private final MechanicService mechanicService;
    private final ServiceService serviceService;
    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;

    public AdminController(
            DashboardService dashboardService,
            UserService userService,
            MechanicService mechanicService,
            ServiceService serviceService,
            BookingService bookingService,
            VehicleService vehicleService,
            PaymentService paymentService,
            ReviewService reviewService) {
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.mechanicService = mechanicService;
        this.serviceService = serviceService;
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.adminDashboard();
    }

    @GetMapping("/reports")
    public Map<String, Object> reports() {
        return dashboardService.adminReports();
    }

    @GetMapping("/customers")
    public List<User> customers() {
        return userService.getCustomers();
    }

    @PutMapping("/customers/{id}/status")
    public User customerStatus(@PathVariable Long id, @RequestParam boolean status) {
        return userService.updateCustomerStatus(id, status);
    }

    @GetMapping("/vehicles")
    public List<Vehicle> vehicles() {
        return vehicleService.getAllVehicles();
    }

    @GetMapping("/mechanics")
    public List<Mechanic> mechanics() {
        return mechanicService.getAllMechanics();
    }

    @GetMapping("/mechanics/available")
    public List<Mechanic> availableMechanics() {
        return mechanicService.getAvailableMechanics();
    }

    @PostMapping("/mechanics")
    public Mechanic createMechanic(@RequestBody MechanicRequest request) {
        return mechanicService.createMechanic(request);
    }

    @PutMapping("/mechanics/{id}")
    public Mechanic updateMechanic(@PathVariable Long id, @RequestBody MechanicRequest request) {
        return mechanicService.updateMechanic(id, request);
    }

    @PutMapping("/mechanics/{id}/availability")
    public Mechanic mechanicAvailability(@PathVariable Long id, @RequestParam boolean available) {
        return mechanicService.setAvailability(id, available);
    }

    @PutMapping("/mechanics/{id}/status")
    public Mechanic mechanicStatus(@PathVariable Long id, @RequestParam boolean status) {
        return mechanicService.setStatus(id, status);
    }

    @GetMapping("/services")
    public List<Service> services() {
        return serviceService.getAllServices();
    }

    @PostMapping("/services")
    public Service addService(@RequestBody Service service) {
        return serviceService.addService(service);
    }

    @PutMapping("/services/{id}")
    public Service updateService(@PathVariable Long id, @RequestBody Service service) {
        return serviceService.updateService(id, service);
    }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings() {
        return bookingService.getAllBookings();
    }

    @GetMapping("/bookings/pending")
    public List<BookingResponse> pendingBookings() {
        return bookingService.getPendingBookings();
    }

    @PostMapping("/bookings/{id}/assign")
    public BookingResponse assign(
            @PathVariable Long id,
            @RequestBody AssignMechanicRequest request) {
        return bookingService.assignMechanic(id, request.getMechanicId());
    }

    @GetMapping("/payments")
    public List<Payment> payments() {
        return paymentService.getAllPayments();
    }

    @GetMapping("/reviews")
    public List<Review> reviews() {
        return reviewService.getAllReviews();
    }
}
