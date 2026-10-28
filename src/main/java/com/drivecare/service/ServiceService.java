package com.drivecare.service;

import com.drivecare.entity.Service;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.ServiceRepository;
import org.springframework.http.HttpStatus;

import java.util.List;

@org.springframework.stereotype.Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public Service addService(Service service) {
        if (serviceRepository.existsByServiceName(service.getServiceName())) {
            throw new ApiException("A service with this name already exists");
        }
        service.setStatus(true);
        return serviceRepository.save(service);
    }

    public Service updateService(Long id, Service request) {
        Service service = getServiceById(id);
        service.setServiceName(request.getServiceName());
        service.setDescription(request.getDescription());
        service.setPrice(request.getPrice());
        service.setStatus(request.isStatus());
        return serviceRepository.save(service);
    }

    public List<Service> getAllServices() {
        return serviceRepository.findAll();
    }

    public List<Service> getActiveServices() {
        return serviceRepository.findByStatusTrue();
    }

    public Service getServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ApiException("Service not found", HttpStatus.NOT_FOUND));
    }
}
