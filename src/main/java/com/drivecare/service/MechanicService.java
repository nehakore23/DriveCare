package com.drivecare.service;

import com.drivecare.dto.MechanicRequest;
import com.drivecare.entity.Mechanic;
import com.drivecare.repository.MechanicRepository;
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
            throw new RuntimeException("Mechanic email already registered");
        }

        Mechanic mechanic = new Mechanic();

        mechanic.setName(request.getName());
        mechanic.setEmail(request.getEmail());

        mechanic.setPassword(
                passwordEncoder.encode(request.getPassword())
        );

        mechanic.setPhone(request.getPhone());
        mechanic.setSpecialization(request.getSpecialization());
        mechanic.setExperience(request.getExperience());

        mechanic.setAvailable(true);
        mechanic.setStatus(true);

        return mechanicRepository.save(mechanic);
    }

    public List<Mechanic> getAllMechanics() {
        return mechanicRepository.findAll();
    }

    public Mechanic getMechanicById(Long id) {
        return mechanicRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Mechanic not found"));
    }
}