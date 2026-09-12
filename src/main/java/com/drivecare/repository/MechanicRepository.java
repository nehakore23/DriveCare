package com.drivecare.repository;

import com.drivecare.entity.Mechanic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MechanicRepository
        extends JpaRepository<Mechanic, Long> {

    Optional<Mechanic> findByEmail(String email);

    boolean existsByEmail(String email);
}