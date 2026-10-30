package com.drivecare.controller;

import com.drivecare.entity.Service;
import com.drivecare.service.ServiceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final ServiceService serviceService;

    public PublicController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    @GetMapping("/services")
    public List<Service> services() {
        return serviceService.getActiveServices();
    }
}
