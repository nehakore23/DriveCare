package com.drivecare.config;

import com.drivecare.entity.Mechanic;
import com.drivecare.entity.Role;
import com.drivecare.entity.Service;
import com.drivecare.entity.User;
import com.drivecare.entity.Vehicle;
import com.drivecare.repository.MechanicRepository;
import com.drivecare.repository.ServiceRepository;
import com.drivecare.repository.UserRepository;
import com.drivecare.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MechanicRepository mechanicRepository;
    private final ServiceRepository serviceRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            MechanicRepository mechanicRepository,
            ServiceRepository serviceRepository,
            VehicleRepository vehicleRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mechanicRepository = mechanicRepository;
        this.serviceRepository = serviceRepository;
        this.vehicleRepository = vehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedServices();
        seedDemoCustomer();
        seedDemoMechanic();
    }

    private void seedAdmin() {
        if (userRepository.findByEmail("admin@drivecare.com").isPresent()) {
            return;
        }
        User admin = new User();
        admin.setName("DriveCare Admin");
        admin.setEmail("admin@drivecare.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setPhone("9000000001");
        admin.setRole(Role.ADMIN);
        admin.setStatus(true);
        userRepository.save(admin);
    }

    private void seedServices() {
        addService("General Service", "Full vehicle inspection and general servicing", "2499");
        addService("Oil Change", "Engine oil and filter replacement", "1499");
        addService("Brake Service", "Brake pads, fluid and performance check", "1999");
        addService("AC Service", "AC gas, filter and cooling checkup", "1799");
    }

    private void addService(String name, String description, String price) {
        if (serviceRepository.existsByServiceName(name)) {
            return;
        }
        Service service = new Service();
        service.setServiceName(name);
        service.setDescription(description);
        service.setPrice(new BigDecimal(price));
        service.setStatus(true);
        serviceRepository.save(service);
    }

    private void seedDemoCustomer() {
        if (userRepository.findByEmail("neha@drivecare.com").isPresent()) {
            return;
        }
        User neha = new User();
        neha.setName("Neha");
        neha.setEmail("neha@drivecare.com");
        neha.setPassword(passwordEncoder.encode("neha123"));
        neha.setPhone("9876543210");
        neha.setRole(Role.CUSTOMER);
        neha.setStatus(true);
        userRepository.save(neha);

        Vehicle creta = new Vehicle();
        creta.setVehicleNumber("MH12AB1234");
        creta.setBrand("Hyundai");
        creta.setModel("Creta");
        creta.setYear(2023);
        creta.setFuelType("Petrol");
        creta.setUser(neha);
        vehicleRepository.save(creta);
    }

    private void seedDemoMechanic() {
        if (mechanicRepository.findByEmail("rahul@drivecare.com").isPresent()) {
            return;
        }
        Mechanic rahul = new Mechanic();
        rahul.setName("Rahul");
        rahul.setEmail("rahul@drivecare.com");
        rahul.setPassword(passwordEncoder.encode("rahul123"));
        rahul.setPhone("9123456780");
        rahul.setSpecialization("General Service");
        rahul.setExperience(5);
        rahul.setAvailable(true);
        rahul.setStatus(true);
        mechanicRepository.save(rahul);
    }
}
