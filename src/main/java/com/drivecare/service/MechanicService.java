package com.drivecare.service;

import com.drivecare.dto.MechanicRequest;
import com.drivecare.entity.Mechanic;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.MechanicRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MechanicService {

    private final MechanicRepository mechanicRepository;
    private final PasswordEncoder passwordEncoder;

    public MechanicService(MechanicRepository mechanicRepository,
                           PasswordEncoder passwordEncoder) {
        this.mechanicRepository = mechanicRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Mechanic createMechanic(MechanicRequest request) {
        if (mechanicRepository.existsByEmail(request.getEmail())) {
            throw new ApiException("Mechanic email already registered");
        }

        Mechanic mechanic = new Mechanic();
        mechanic.setName(request.getName());
        mechanic.setEmail(request.getEmail());
        mechanic.setPassword(passwordEncoder.encode(request.getPassword()));
        mechanic.setPhone(request.getPhone());
        mechanic.setSpecialization(request.getSpecialization());
        mechanic.setExperience(request.getExperience());
        mechanic.setAvailable(true);
        mechanic.setStatus(true);
        return mechanicRepository.save(mechanic);
    }

    public Mechanic updateMechanic(Long id, MechanicRequest request) {
        Mechanic mechanic = getMechanicById(id);
        mechanic.setName(request.getName());
        mechanic.setPhone(request.getPhone());
        mechanic.setSpecialization(request.getSpecialization());
        mechanic.setExperience(request.getExperience());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            mechanic.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return mechanicRepository.save(mechanic);
    }

    public Mechanic setAvailability(Long id, boolean available) {
        Mechanic mechanic = getMechanicById(id);
        mechanic.setAvailable(available);
        return mechanicRepository.save(mechanic);
    }

    public Mechanic setStatus(Long id, boolean status) {
        Mechanic mechanic = getMechanicById(id);
        mechanic.setStatus(status);
        return mechanicRepository.save(mechanic);
    }

    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }

    public List<Mechanic> getAvailableMechanics() {
        return mechanicRepository.findAll().stream()
                .filter(m -> m.isStatus() && m.isAvailable())
                .toList();
    }

    public Mechanic getMechanicById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() -> new ApiException("Mechanic not found", HttpStatus.NOT_FOUND));
    }
}
