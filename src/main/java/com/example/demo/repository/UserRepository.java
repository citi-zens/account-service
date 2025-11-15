package com.example.demo.repository;

import com.example.demo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// Spring Data JPA repository for User entity
public interface UserRepository extends JpaRepository<User, Long> {
    // Find a user by their unique email
    Optional<User> findByEmail(String email);
}