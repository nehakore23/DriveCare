package com.drivecare.repository;

import com.drivecare.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByMechanicIdOrderByCreatedAtDesc(Long mechanicId);

    long countByMechanicIdAndReadFlagFalse(Long mechanicId);
}
