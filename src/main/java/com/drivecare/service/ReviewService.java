package com.drivecare.service;

import com.drivecare.dto.ReviewRequest;
import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.Review;
import com.drivecare.entity.User;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.BookingRepository;
import com.drivecare.repository.ReviewRepository;
import com.drivecare.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public Review addReview(ReviewRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ApiException("Booking not found", HttpStatus.NOT_FOUND));

        if (!booking.getCustomer().getId().equals(user.getId())) {
            throw new ApiException("You can review only your own completed service", HttpStatus.FORBIDDEN);
        }
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw new ApiException("Review is allowed only after service is completed");
        }
        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new ApiException("Rating must be between 1 and 5");
        }
        if (reviewRepository.findByBookingId(booking.getId()).isPresent()) {
            throw new ApiException("Review already submitted for this booking");
        }

        Review review = new Review();
        review.setBooking(booking);
        review.setCustomer(user);
        review.setMechanic(booking.getMechanic());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(LocalDateTime.now());
        return reviewRepository.save(review);
    }

    public List<Review> getCustomerReviews(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return reviewRepository.findByCustomerIdOrderByCreatedAtDesc(user.getId());
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}
