package com.drivecare.service;

import com.drivecare.entity.Booking;
import com.drivecare.entity.Notification;
import com.drivecare.entity.Mechanic;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.MechanicRepository;
import com.drivecare.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MechanicRepository mechanicRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            MechanicRepository mechanicRepository) {
        this.notificationRepository = notificationRepository;
        this.mechanicRepository = mechanicRepository;
    }

    public void notifyAssignment(Booking booking) {
        Notification notification = new Notification();
        notification.setMechanic(booking.getMechanic());
        notification.setBooking(booking);
        notification.setTitle("New job assigned");
        notification.setMessage(
                "Customer: " + booking.getCustomer().getName()
                        + " | Vehicle: " + booking.getVehicle().getBrand() + " " + booking.getVehicle().getModel()
                        + " | Service: " + booking.getService().getServiceName()
                        + " | Date: " + booking.getBookingDate()
                        + " | Time: " + booking.getBookingTime()
        );
        notificationRepository.save(notification);
    }

    public List<Notification> getMechanicNotifications(String email) {
        Mechanic mechanic = mechanicRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Mechanic not found", HttpStatus.NOT_FOUND));
        return notificationRepository.findByMechanicIdOrderByCreatedAtDesc(mechanic.getId());
    }

    public Notification markRead(Long id, String email) {
        Mechanic mechanic = mechanicRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Mechanic not found", HttpStatus.NOT_FOUND));
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ApiException("Notification not found", HttpStatus.NOT_FOUND));
        if (!notification.getMechanic().getId().equals(mechanic.getId())) {
            throw new ApiException("You cannot update this notification", HttpStatus.FORBIDDEN);
        }
        notification.setReadFlag(true);
        return notificationRepository.save(notification);
    }
}
