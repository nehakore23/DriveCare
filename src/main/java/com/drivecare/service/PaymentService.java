
package com.drivecare.service;

import com.drivecare.dto.RazorpayOrderResponse;
import com.drivecare.dto.RazorpayVerifyRequest;
import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.Payment;
import com.drivecare.entity.PaymentStatus;
import com.drivecare.entity.User;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.BookingRepository;
import com.drivecare.repository.PaymentRepository;
import com.drivecare.repository.UserRepository;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    private final String keyId;
    private final String keySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            @Value("${razorpay.key.id}") String keyId,
            @Value("${razorpay.key.secret}") String keySecret) {

        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    // Create Razorpay Order
    @Transactional
    public RazorpayOrderResponse createOrder(Long bookingId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found", HttpStatus.NOT_FOUND));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ApiException(
                        "Booking not found", HttpStatus.NOT_FOUND));

        // Verify booking ownership
        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new ApiException(
                    "You cannot pay for another customer's booking",
                    HttpStatus.FORBIDDEN);
        }

        // Payment only after service completion
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ApiException(
                    "Payment is allowed only after service completion");
        }

        BigDecimal amount = booking.getTotalPrice();

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0
                || amount.stripTrailingZeros().scale() > 2) {
            throw new ApiException("Invalid booking amount");
        }

        long amountInPaise;

        try {
            amountInPaise = amount
                    .movePointRight(2)
                    .longValueExact();
        } catch (ArithmeticException e) {
            throw new ApiException("Invalid payment amount");
        }

        // Check existing payment
        Payment payment = paymentRepository
                .findByBookingId(bookingId)
                .orElse(null);

        if (payment != null) {

            if (payment.getStatus() == PaymentStatus.PAID) {
                throw new ApiException(
                        "This booking has already been paid",
                        HttpStatus.CONFLICT);
            }

            // Reuse existing pending Razorpay order
            if (payment.getRazorpayOrderId() != null) {
                return new RazorpayOrderResponse(
                        keyId,
                        payment.getRazorpayOrderId(),
                        amountInPaise,
                        "INR",
                        bookingId,
                        payment.getId());
            }

        } else {

            payment = new Payment();
            payment.setBooking(booking);
            payment.setCustomer(user);
            payment.setAmount(amount);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setMethod("RAZORPAY");
            payment.setReceiptNumber(
                    "DC-" + UUID.randomUUID()
                            .toString().substring(0, 8).toUpperCase());

            payment = paymentRepository.save(payment);
        }

        try {

            RazorpayClient client =
                    new RazorpayClient(keyId, keySecret);

            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put(
                    "receipt", "DC-BOOKING-" + bookingId);

            com.razorpay.Order order =
                    client.orders.create(orderRequest);

            String orderId = order.get("id");

            payment.setRazorpayOrderId(orderId);
            payment.setMethod("RAZORPAY");

            paymentRepository.save(payment);

            return new RazorpayOrderResponse(
                    keyId,
                    orderId,
                    amountInPaise,
                    "INR",
                    bookingId,
                    payment.getId());

        } catch (RazorpayException e) {

            throw new ApiException(
                    "Unable to create Razorpay order",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    // Verify Razorpay Payment
    @Transactional
    public Payment verifyPayment(
            RazorpayVerifyRequest request,
            String email) {

        if (request == null
                || request.getRazorpayOrderId() == null
                || request.getRazorpayPaymentId() == null
                || request.getRazorpaySignature() == null) {

            throw new ApiException("Payment verification details are missing");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found", HttpStatus.NOT_FOUND));

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new ApiException(
                        "Payment order not found", HttpStatus.NOT_FOUND));

        if (!payment.getCustomer().getId().equals(user.getId())) {
            throw new ApiException(
                    "Unauthorized payment verification",
                    HttpStatus.FORBIDDEN);
        }

        // Verify signature
        try {

            JSONObject options = new JSONObject();

            options.put(
                    "razorpay_order_id",
                    request.getRazorpayOrderId());

            options.put(
                    "razorpay_payment_id",
                    request.getRazorpayPaymentId());

            options.put(
                    "razorpay_signature",
                    request.getRazorpaySignature());

            boolean signatureValid =
                    Utils.verifyPaymentSignature(options, keySecret);

            if (!signatureValid) {
                throw new ApiException(
                        "Invalid Razorpay payment signature",
                        HttpStatus.BAD_REQUEST);
            }

            // Idempotency: same successful payment can be verified again
            if (payment.getStatus() == PaymentStatus.PAID) {

                if (request.getRazorpayPaymentId()
                        .equals(payment.getRazorpayPaymentId())) {
                    return payment;
                }

                throw new ApiException(
                        "This booking has already been paid",
                        HttpStatus.CONFLICT);
            }

            // Fetch payment status directly from Razorpay
            RazorpayClient client =
                    new RazorpayClient(keyId, keySecret);

            com.razorpay.Payment razorpayPayment =
                    client.payments.fetch(
                            request.getRazorpayPaymentId());

            String razorpayStatus =
                    String.valueOf(razorpayPayment.get("status"));

            String razorpayOrderId =
                    String.valueOf(razorpayPayment.get("order_id"));

            long razorpayAmount =
                    new BigDecimal(
                            String.valueOf(razorpayPayment.get("amount")))
                            .longValueExact();

            long expectedAmount =
                    payment.getAmount()
                            .movePointRight(2)
                            .longValueExact();

            if (!"captured".equalsIgnoreCase(razorpayStatus)) {
                throw new ApiException(
                        "Payment is not captured yet",
                        HttpStatus.BAD_REQUEST);
            }

            if (!request.getRazorpayOrderId().equals(razorpayOrderId)
                    || razorpayAmount != expectedAmount) {

                throw new ApiException(
                        "Payment details do not match",
                        HttpStatus.BAD_REQUEST);
            }

            // Mark payment as PAID only after verification
            payment.setStatus(PaymentStatus.PAID);
            payment.setMethod("RAZORPAY");
            payment.setRazorpayPaymentId(
                    request.getRazorpayPaymentId());
            payment.setPaidAt(LocalDateTime.now());

            return paymentRepository.save(payment);

        } catch (RazorpayException e) {

            throw new ApiException(
                    "Unable to verify Razorpay payment",
                    HttpStatus.BAD_GATEWAY);
        }
    }

    public List<Payment> getCustomerPayments(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found", HttpStatus.NOT_FOUND));

        return paymentRepository
                .findByCustomerIdOrderByIdDesc(user.getId());
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
