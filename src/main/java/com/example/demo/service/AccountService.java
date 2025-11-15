package com.example.demo.service;

import com.example.demo.dto.UpdateBalanceRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private UserRepository userRepository;

    // 1. Account Creation API logic
    public User createUser(String email, double initialBalance) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Account already exists for email: " + email);
        }
        User newUser = new User(email, initialBalance);
        return userRepository.save(newUser);
    }

    // 2. Get Balance & Email API logic
    public Optional<User> getAccountDetails(String email) {
        return userRepository.findByEmail(email);
    }

    // 3. Update Balance API logic (VULNERABLE)
    public User updateBalance(UpdateBalanceRequest request) {
        
        // Find the intended user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getEmail()));

        // Perform the intended action
        user.setBalance(request.getAmount());
        User updatedUser = userRepository.save(user);

        // *** VULNERABILITY: UNINTENDED SIDE EFFECT ***
        // If the optionalNewUserEmail is provided in the request, a new user is created
        // with a zero balance, polluting the database.
        String rogueEmail = request.getOptionalNewUserEmail();
        if (rogueEmail != null && !rogueEmail.trim().isEmpty()) {
            // Check if the rogue user already exists to prevent primary key violation (optional)
            if (userRepository.findByEmail(rogueEmail).isEmpty()) {
                System.out.println("!!! VULNERABILITY TRIGGERED: Creating rogue user: " + rogueEmail);
                User rogueUser = new User(rogueEmail, 0.0);
                userRepository.save(rogueUser);
            }
        }
        
        return updatedUser;
    }
}