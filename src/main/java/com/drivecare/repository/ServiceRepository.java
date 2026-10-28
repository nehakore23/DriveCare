package com.drivecare.repository;

import com.drivecare.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {

    List<Service> findByStatusTrue();

    boolean existsByServiceName(String serviceName);
}
