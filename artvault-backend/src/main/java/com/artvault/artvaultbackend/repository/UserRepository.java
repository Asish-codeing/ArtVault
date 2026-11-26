package com.artvault.artvaultbackend.repository;

import com.artvault.artvaultbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCaseOrEmailIgnoreCase(
            String username, String email
    );

    // Find a user by ID
    Optional<User> findById(Long id);

    // Find users by role
    List<User> findByRole(String role);

    // Check whether a user is an admin
    boolean existsByIdAndRole(Long id, String role);
}