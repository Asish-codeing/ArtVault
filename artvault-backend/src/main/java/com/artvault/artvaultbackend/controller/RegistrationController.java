package com.artvault.artvaultbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.service.ActivityService;
import com.artvault.artvaultbackend.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class RegistrationController {

    private final UserService userService;
    private final ActivityService activityService;

    public RegistrationController(
            UserService userService,
            ActivityService activityService) {

        this.userService = userService;
        this.activityService = activityService;
    }

    // USER REGISTRATION
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @RequestBody(required = false) RegisterRequest request) {

        // Check whether request exists
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Registration details are required."
            );
        }

        // Create account
        User user = userService.registerUser(
                request.username(),
                request.email(),
                request.password()
        );

        // Record successful registration
        activityService.logActivity(
                user.getId(),
                user.getUsername(),
                "USER_REGISTERED",
                "New user registered"
        );

        // Return successful response
        RegisterResponse response = new RegisterResponse(
                "Registration successful!",
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Registration request
    public record RegisterRequest(
            String username,
            String email,
            String password
    ) {
    }

    // Registration response
    public record RegisterResponse(
            String message,
            Long userId,
            String username,
            String email
    ) {
    }
}