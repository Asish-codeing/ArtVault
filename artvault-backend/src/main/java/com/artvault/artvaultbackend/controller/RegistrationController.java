package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.service.UserService;
import com.artvault.artvaultbackend.service.ActivityService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody RegisterRequest request) {

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

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(
                        "Registration successful!",
                        user.getId(),
                        user.getUsername(),
                        user.getEmail()
                ));
    }

    public record RegisterRequest(
            String username,
            String email,
            String password
    ) {}

    public record RegisterResponse(
            String message,
            Long userId,
            String username,
            String email
    ) {}
}