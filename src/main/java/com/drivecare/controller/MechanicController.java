package com.drivecare.controller;

import com.drivecare.dto.BookingResponse;
import com.drivecare.dto.LoginRequest;
import com.drivecare.dto.LoginResponse;
import com.drivecare.dto.MechanicJobUpdateRequest;
import com.drivecare.entity.BookingStatus;
import com.drivecare.entity.Notification;
import com.drivecare.service.BookingService;
import com.drivecare.service.DashboardService;
import com.drivecare.service.MechanicAuthService;
import com.drivecare.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mechanic")
public class MechanicController {

    private final MechanicAuthService mechanicAuthService;
    private final BookingService bookingService;
    private final DashboardService dashboardService;
    private final NotificationService notificationService;

    public MechanicController(
            MechanicAuthService mechanicAuthService,
            BookingService bookingService,
            DashboardService dashboardService,
            NotificationService notificationService) {
        this.mechanicAuthService = mechanicAuthService;
        this.bookingService = bookingService;
        this.dashboardService = dashboardService;
        this.notificationService = notificationService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return mechanicAuthService.login(request);
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        return dashboardService.mechanicDashboard(authentication.getName());
    }

    @GetMapping("/jobs")
    public List<BookingResponse> assignedJobs(Authentication authentication) {
        return bookingService.getMechanicJobs(authentication.getName());
    }

    @GetMapping("/jobs/today")
    public List<BookingResponse> today(Authentication authentication) {
        return bookingService.getTodayJobs(authentication.getName());
    }

    @GetMapping("/jobs/assigned")
    public List<BookingResponse> assigned(Authentication authentication) {
        return bookingService.getMechanicJobsByStatus(authentication.getName(), BookingStatus.ASSIGNED);
    }

    @GetMapping("/jobs/in-progress")
    public List<BookingResponse> inProgress(Authentication authentication) {
        return bookingService.getMechanicJobsByStatus(authentication.getName(), BookingStatus.IN_PROGRESS);
    }

    @GetMapping("/jobs/completed")
    public List<BookingResponse> completed(Authentication authentication) {
        return bookingService.getMechanicJobsByStatus(authentication.getName(), BookingStatus.COMPLETED);
    }

    @GetMapping("/schedule")
    public List<BookingResponse> schedule(Authentication authentication) {
        return bookingService.getMechanicJobs(authentication.getName());
    }

    @PostMapping("/jobs/{id}/start")
    public BookingResponse start(@PathVariable Long id, Authentication authentication) {
        return bookingService.startJob(id, authentication.getName());
    }

    @PutMapping("/jobs/{id}/notes")
    public BookingResponse notes(
            @PathVariable Long id,
            @RequestBody MechanicJobUpdateRequest request,
            Authentication authentication) {
        return bookingService.updateJobNotes(id, request, authentication.getName());
    }

    @PostMapping("/jobs/{id}/complete")
    public BookingResponse complete(@PathVariable Long id, Authentication authentication) {
        return bookingService.completeJob(id, authentication.getName());
    }

    @GetMapping("/notifications")
    public List<Notification> notifications(Authentication authentication) {
        return notificationService.getMechanicNotifications(authentication.getName());
    }

    @PutMapping("/notifications/{id}/read")
    public Notification markRead(@PathVariable Long id, Authentication authentication) {
        return notificationService.markRead(id, authentication.getName());
    }
}
