package com.drivecare.controller;

import com.drivecare.dto.MechanicRequest;
import com.drivecare.entity.Mechanic;
import com.drivecare.service.MechanicService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/mechanics")
public class AdminMechanicController {

    private final MechanicService mechanicService;

    public AdminMechanicController(MechanicService mechanicService) {
        this.mechanicService = mechanicService;
    }

    @PostMapping
    public Mechanic createMechanic(
            @RequestBody MechanicRequest request) {

        return mechanicService.createMechanic(request);
    }

    @GetMapping
    public List<Mechanic> getAllMechanics() {
        return mechanicService.getAllMechanics();
    }

    @GetMapping("/{id}")
    public Mechanic getMechanicById(
            @PathVariable Long id) {

        return mechanicService.getMechanicById(id);
    }
}