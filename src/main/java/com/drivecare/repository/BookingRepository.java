package com.drivecare.repository;

import com.drivecare.entity.Booking;
import com.drivecare.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByCustomerIdOrderByBookingDateDescBookingTimeDesc(Long customerId);

    List<Booking> findByMechanicIdOrderByBookingDateAscBookingTimeAsc(Long mechanicId);

    List<Booking> findByMechanicIdAndBookingDate(Long mechanicId, LocalDate date);

    List<Booking> findByMechanicIdAndStatus(Long mechanicId, BookingStatus status);

    List<Booking> findByCustomerIdAndStatusIn(Long customerId, List<BookingStatus> statuses);

    List<Booking> findByStatus(BookingStatus status);

    long countByStatus(BookingStatus status);

    @Query("SELECT b.service.serviceName, COUNT(b) FROM Booking b GROUP BY b.service.serviceName ORDER BY COUNT(b) DESC")
    List<Object[]> countBookingsByService();

    @Query("SELECT MONTH(b.bookingDate), COUNT(b), COALESCE(SUM(b.totalPrice), 0) "
            + "FROM Booking b WHERE YEAR(b.bookingDate) = :year GROUP BY MONTH(b.bookingDate) ORDER BY MONTH(b.bookingDate)")
    List<Object[]> monthlyBookingStats(int year);

    @Query("SELECT MONTH(b.bookingDate), COALESCE(SUM(b.totalPrice), 0) "
            + "FROM Booking b WHERE YEAR(b.bookingDate) = :year AND b.status = com.drivecare.entity.BookingStatus.COMPLETED "
            + "GROUP BY MONTH(b.bookingDate) ORDER BY MONTH(b.bookingDate)")
    List<Object[]> monthlyRevenueStats(int year);

    @Query("SELECT m.id, m.name, COUNT(b) FROM Booking b JOIN b.mechanic m "
            + "WHERE b.status = com.drivecare.entity.BookingStatus.COMPLETED GROUP BY m.id, m.name")
    List<Object[]> completedJobsByMechanic();
}
