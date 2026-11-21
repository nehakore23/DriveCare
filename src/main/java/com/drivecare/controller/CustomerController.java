
package com.drivecare.controller;

import com.drivecare.dto.BookingRequest;
import com.drivecare.dto.BookingResponse;
import com.drivecare.dto.ReviewRequest;
import com.drivecare.dto.RazorpayOrderResponse;
import com.drivecare.dto.RazorpayVerifyRequest;

import com.drivecare.entity.Payment;
import com.drivecare.entity.Review;
import com.drivecare.entity.Service;
import com.drivecare.entity.Vehicle;

import com.drivecare.service.BookingService;
import com.drivecare.service.DashboardService;
import com.drivecare.service.PaymentService;
import com.drivecare.service.ReviewService;
import com.drivecare.service.ServiceService;
import com.drivecare.service.VehicleService;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final VehicleService vehicleService;
    private final ServiceService serviceService;
    private final BookingService bookingService;
    private final DashboardService dashboardService;
    private final PaymentService paymentService;
    private final ReviewService reviewService;

    public CustomerController(
            VehicleService vehicleService,
            ServiceService serviceService,
            BookingService bookingService,
            DashboardService dashboardService,
            PaymentService paymentService,
            ReviewService reviewService) {

        this.vehicleService = vehicleService;
        this.serviceService = serviceService;
        this.bookingService = bookingService;
        this.dashboardService = dashboardService;
        this.paymentService = paymentService;
        this.reviewService = reviewService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return dashboardService.customerDashboard(authentication.getName());
    }

    // Add vehicle
    @PostMapping("/vehicles")
    public Vehicle addVehicle(
            @RequestBody Vehicle vehicle,
            Authentication authentication) {

        return vehicleService.addVehicle(
                vehicle,
                authentication.getName()
        );
    }

    // Get customer's vehicles
    @GetMapping("/vehicles")
    public List<Vehicle> myVehicles(Authentication authentication) {
        return vehicleService.getMyVehicles(authentication.getName());
    }

    // Upload vehicle image
    @PostMapping(
            value = "/vehicles/{id}/image",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Vehicle uploadVehicleImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        return vehicleService.uploadVehicleImage(
                id,
                file,
                authentication.getName()
        );
    }

    @GetMapping("/services")
    public List<Service> services() {
        return serviceService.getActiveServices();
    }

    @PostMapping("/bookings")
    public BookingResponse book(
            @RequestBody BookingRequest request,
            Authentication authentication) {

        return bookingService.createBooking(
                request,
                authentication.getName()
        );
    }

    @GetMapping("/bookings")
    public List<BookingResponse> bookings(Authentication authentication) {
        return bookingService.getCustomerBookings(authentication.getName());
    }

    @GetMapping("/bookings/{id}")
    public BookingResponse booking(
            @PathVariable Long id,
            Authentication authentication) {

        return bookingService.getCustomerBooking(
                id,
                authentication.getName()
        );
    }

    @PostMapping("/bookings/{id}/cancel")
    public BookingResponse cancel(
            @PathVariable Long id,
            Authentication authentication) {

        return bookingService.cancelBooking(
                id,
                authentication.getName()
        );
    }

    @PostMapping("/bookings/{id}/payment/order")
    public RazorpayOrderResponse createPaymentOrder(
            @PathVariable Long id,
            Authentication authentication) {

        return paymentService.createPaymentOrder(
                id,
                authentication.getName()
        );
    }

    @PostMapping("/payments/verify")
    public Payment verifyPayment(
            @RequestBody RazorpayVerifyRequest request,
            Authentication authentication) {

        return paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature(),
                authentication.getName()
        );
    }

    @GetMapping("/payments")
    public List<Payment> payments(Authentication authentication) {
        return paymentService.getCustomerPayments(authentication.getName());
    }

    @PostMapping("/reviews")
    public Review review(
            @RequestBody ReviewRequest request,
            Authentication authentication) {

        return reviewService.addReview(
                request,
                authentication.getName()
        );
    }

    @GetMapping("/reviews")
    public List<Review> reviews(Authentication authentication) {
        return reviewService.getCustomerReviews(authentication.getName());
    }
}
