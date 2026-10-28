package com.drivecare.repository;

import com.drivecare.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    Optional<Review> findByBookingId(Long bookingId);

    List<Review> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    @Query("SELECT r.mechanic.id, AVG(r.rating) FROM Review r WHERE r.mechanic IS NOT NULL GROUP BY r.mechanic.id")
    List<Object[]> averageRatingByMechanic();
}
