package com.drivecare.service;

import com.drivecare.dto.RazorpayOrderResponse;
import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.Payment;
import com.drivecare.entity.PaymentStatus;
import com.drivecare.entity.User;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.BookingRepository;
import com.drivecare.repository.PaymentRepository;
import com.drivecare.repository.UserRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    public PaymentService(
            PaymentRepository paymentRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository) {

        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

 // Create Pending Payment after mechanic completes the job
 public Payment createPendingPayment(Booking booking) {

     // Check if payment already exists
     Payment existingPayment = paymentRepository
             .findByBooking_Id(booking.getId())
             .orElse(null);

     if (existingPayment != null) {
         return existingPayment;
     }

     Payment payment = new Payment();

     payment.setBooking(booking);
     payment.setCustomer(booking.getCustomer());
     payment.setAmount(booking.getTotalPrice());
     payment.setStatus(PaymentStatus.PENDING);
     payment.setMethod("RAZORPAY");

     payment.setReceiptNumber(
             "DC-" + UUID.randomUUID()
                     .toString()
                     .substring(0, 8)
                     .toUpperCase()
     );

     return paymentRepository.save(payment);
 }

    // Create Razorpay Order
    public RazorpayOrderResponse createPaymentOrder(
            Long bookingId,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ApiException(
                                "Booking not found",
                                HttpStatus.NOT_FOUND
                        ));

        // Check booking belongs to logged-in customer
        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new ApiException(
                    "You are not authorized to pay for this booking",
                    HttpStatus.FORBIDDEN
            );
        }

        // Payment allowed only after service completion
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ApiException(
                    "Payment is allowed only after service completion",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Validate amount
        if (booking.getTotalPrice() == null ||
                booking.getTotalPrice().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ApiException(
                    "Invalid booking amount",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Check existing payment
        Payment payment = paymentRepository
                .findByBooking_Id(bookingId)
                .orElse(null);

        // Already paid
        if (payment != null &&
                payment.getStatus() == PaymentStatus.PAID) {

            throw new ApiException(
                    "Payment already completed for this booking",
                    HttpStatus.BAD_REQUEST
            );
        }

        try {

            // Convert INR to paise
            long amountInPaise = booking.getTotalPrice()
                    .multiply(BigDecimal.valueOf(100))
                    .longValueExact();

            /*
             * If a pending payment already has a Razorpay order,
             * reuse that order instead of creating a duplicate order.
             */
            if (payment != null &&
                    payment.getRazorpayOrderId() != null) {

                return new RazorpayOrderResponse(
                        razorpayKeyId,
                        payment.getRazorpayOrderId(),
                        bookingId,
                        payment.getId(),
                        amountInPaise,
                        "INR"
                );
            }

            // Create local payment record
            if (payment == null) {

                payment = new Payment();

                payment.setBooking(booking);
                payment.setCustomer(user);
                payment.setAmount(booking.getTotalPrice());
                payment.setStatus(PaymentStatus.PENDING);
                payment.setMethod("RAZORPAY");

                payment.setReceiptNumber(
                        "DC-" +
                                UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase()
                );

                payment = paymentRepository.save(payment);
            }

            // Create Razorpay client
            RazorpayClient razorpayClient =
                    new RazorpayClient(
                            razorpayKeyId,
                            razorpayKeySecret
                    );

            // Razorpay order request
            JSONObject orderRequest = new JSONObject();

            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put(
                    "receipt",
                    "booking_" + bookingId
            );

            // Create Razorpay order
            Order razorpayOrder =
                    razorpayClient.orders.create(orderRequest);

            String razorpayOrderId =
                    razorpayOrder.get("id");

            // Save Razorpay Order ID
            payment.setRazorpayOrderId(razorpayOrderId);

            paymentRepository.save(payment);

            // Return clean response to frontend
            return new RazorpayOrderResponse(
                    razorpayKeyId,
                    razorpayOrderId,
                    bookingId,
                    payment.getId(),
                    amountInPaise,
                    "INR"
            );

        } catch (RazorpayException e) {

            throw new ApiException(
                    "Unable to create Razorpay order",
                    HttpStatus.BAD_GATEWAY
            );

        } catch (ArithmeticException e) {

            throw new ApiException(
                    "Invalid payment amount",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // Verify Razorpay Payment
    public Payment verifyPayment(
            String razorpayOrderId,
            String razorpayPaymentId,
            String razorpaySignature,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        Payment payment = paymentRepository
                .findByCustomerIdOrderByIdDesc(user.getId())
                .stream()
                .filter(p ->
                        razorpayOrderId.equals(
                                p.getRazorpayOrderId()
                        ))
                .findFirst()
                .orElseThrow(() ->
                        new ApiException(
                                "Payment order not found",
                                HttpStatus.NOT_FOUND
                        ));

        // Already paid
        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }

        // Verify Razorpay signature
        try {

            String payload =
                    razorpayOrderId + "|" + razorpayPaymentId;

            boolean isValid = Utils.verifySignature(
                    payload,
                    razorpaySignature,
                    razorpayKeySecret
            );

            if (!isValid) {

                throw new ApiException(
                        "Invalid Razorpay payment signature",
                        HttpStatus.BAD_REQUEST
                );
            }

            // Mark payment as paid only after successful verification
            payment.setRazorpayPaymentId(
                    razorpayPaymentId
            );

            payment.setStatus(
                    PaymentStatus.PAID
            );

            payment.setMethod("RAZORPAY");

            payment.setPaidAt(
                    LocalDateTime.now()
            );

            if (payment.getReceiptNumber() == null) {

                payment.setReceiptNumber(
                        "DC-" +
                                UUID.randomUUID()
                                        .toString()
                                        .substring(0, 8)
                                        .toUpperCase()
                );
            }

            return paymentRepository.save(payment);

        } catch (RazorpayException e) {

            throw new ApiException(
                    "Unable to verify Razorpay payment",
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    // Customer payment history
    public List<Payment> getCustomerPayments(
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ApiException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        ));

        return paymentRepository
                .findByCustomerIdOrderByIdDesc(
                        user.getId()
                );
    }

    // Admin payment history
    public List<Payment> getAllPayments() {

        return paymentRepository.findAll();
    }
}