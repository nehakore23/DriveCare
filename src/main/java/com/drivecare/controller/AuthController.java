package com.drivecare.controller;

import com.drivecare.dto.LoginRequest;
import com.drivecare.dto.LoginResponse;
import com.drivecare.dto.RegisterRequest;
import com.drivecare.entity.User;
import com.drivecare.service.AuthService;
import com.drivecare.service.MechanicAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final MechanicAuthService mechanicAuthService;

    public AuthController(AuthService authService, MechanicAuthService mechanicAuthService) {
        this.authService = authService;
        this.mechanicAuthService = mechanicAuthService;
    }

    @PostMapping("/register")
    public User register(@RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/mechanic/login")
    public LoginResponse mechanicLogin(@RequestBody LoginRequest request) {
        return mechanicAuthService.login(request);
    }
}
