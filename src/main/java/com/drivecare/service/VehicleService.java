
package com.drivecare.service;

import com.drivecare.entity.User;
import com.drivecare.entity.Vehicle;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.UserRepository;
import com.drivecare.repository.VehicleRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir:uploads/vehicles}")
    private String uploadDirectory;

    public VehicleService(
            VehicleRepository vehicleRepository,
            UserRepository userRepository) {
        this.vehicleRepository = vehicleRepository;
        this.userRepository = userRepository;
    }

    // Add vehicle
    public Vehicle addVehicle(Vehicle vehicle, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found", HttpStatus.NOT_FOUND));

        vehicle.setUser(user);

        return vehicleRepository.save(vehicle);
    }

    // Get customer's vehicles
    public List<Vehicle> getMyVehicles(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found", HttpStatus.NOT_FOUND));

        return vehicleRepository.findByUserId(user.getId());
    }

    // Upload vehicle image
    public Vehicle uploadVehicleImage(
            Long vehicleId,
            MultipartFile file,
            String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        "User not found", HttpStatus.NOT_FOUND));

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ApiException(
                        "Vehicle not found", HttpStatus.NOT_FOUND));

        // Check vehicle ownership
        if (!vehicle.getUser().getId().equals(user.getId())) {
            throw new ApiException(
                    "You cannot upload image for this vehicle",
                    HttpStatus.FORBIDDEN);
        }

        if (file == null || file.isEmpty()) {
            throw new ApiException(
                    "Please select an image",
                    HttpStatus.BAD_REQUEST);
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp"))) {

            throw new ApiException(
                    "Only JPG, PNG and WEBP images are allowed",
                    HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new ApiException(
                    "Image size must be less than 5 MB",
                    HttpStatus.BAD_REQUEST);
        }

        try {
            Path directory = Paths.get(uploadDirectory)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(directory);

            String extension;

            if (contentType.equals("image/png")) {
                extension = ".png";
            } else if (contentType.equals("image/webp")) {
                extension = ".webp";
            } else {
                extension = ".jpg";
            }

            String fileName = UUID.randomUUID() + extension;

            Path filePath = directory.resolve(fileName).normalize();

            if (!filePath.startsWith(directory)) {
                throw new ApiException(
                        "Invalid image path",
                        HttpStatus.BAD_REQUEST);
            }

            file.transferTo(filePath);

            vehicle.setImageUrl("/uploads/vehicles/" + fileName);

            return vehicleRepository.save(vehicle);

        } catch (IOException e) {
            throw new ApiException(
                    "Unable to save vehicle image",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ApiException(
                        "Vehicle not found", HttpStatus.NOT_FOUND));
    }
}
