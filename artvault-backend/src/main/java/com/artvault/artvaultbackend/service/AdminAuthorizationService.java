package com.artvault.artvaultbackend.service;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.repository.UserRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuthorizationService {

    private final UserRepository userRepository;

    public AdminAuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Verify that the currently logged-in user is an admin.
    public User requireAdmin(HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof Long)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first."
            );
        }

        User user = userRepository.findById((Long) userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User account not found."
                ));

        if (!"ADMIN".equals(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Admin access required."
            );
        }

        return user;
    }
}