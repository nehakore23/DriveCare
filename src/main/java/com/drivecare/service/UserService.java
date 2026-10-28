package com.drivecare.service;

import com.drivecare.entity.Role;
import com.drivecare.entity.User;
import com.drivecare.exception.ApiException;
import com.drivecare.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getCustomers() {
        return userRepository.findByRole(Role.CUSTOMER);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
    }

    public User updateCustomerStatus(Long id, boolean status) {
        User user = getUserById(id);
        if (user.getRole() != Role.CUSTOMER) {
            throw new ApiException("Only customers can be updated here");
        }
        user.setStatus(status);
        return userRepository.save(user);
    }
}
