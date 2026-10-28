package com.drivecare.service;

import com.drivecare.dto.LoginRequest;
import com.drivecare.dto.LoginResponse;
import com.drivecare.entity.Mechanic;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.MechanicRepository;
import com.drivecare.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MechanicAuthService {

    private final MechanicRepository mechanicRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public MechanicAuthService(
            MechanicRepository mechanicRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.mechanicRepository = mechanicRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        Mechanic mechanic = mechanicRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED));

        if (!mechanic.isStatus()) {
            throw new ApiException("Mechanic account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), mechanic.getPassword())) {
            throw new ApiException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        String token = jwtService.generateToken(mechanic.getEmail(), "MECHANIC");
        return new LoginResponse(token, "MECHANIC", mechanic.getName(), mechanic.getId());
    }
}
