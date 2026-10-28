package com.drivecare.service;

import com.drivecare.entity.Booking;
import com.drivecare.entity.Payment;
import com.drivecare.entity.PaymentStatus;
import com.drivecare.entity.User;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.PaymentRepository;
import com.drivecare.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    public Payment createPaidReceipt(Booking booking) {
        return paymentRepository.findByBookingId(booking.getId())
                .orElseGet(() -> {
                    Payment payment = new Payment();
                    payment.setBooking(booking);
                    payment.setCustomer(booking.getCustomer());
                    payment.setAmount(booking.getTotalPrice());
                    payment.setStatus(PaymentStatus.PAID);
                    payment.setMethod("COUNTER / UPI");
                    payment.setReceiptNumber("DC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
                    payment.setPaidAt(LocalDateTime.now());
                    return paymentRepository.save(payment);
                });
    }

    public List<Payment> getCustomerPayments(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return paymentRepository.findByCustomerIdOrderByIdDesc(user.getId());
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
