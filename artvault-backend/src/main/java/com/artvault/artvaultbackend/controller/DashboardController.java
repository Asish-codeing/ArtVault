package com.artvault.artvaultbackend.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = {
        "http://localhost:5500",
        "http://localhost:5500"
    },
    allowCredentials = "true"
)
public class DashboardController {

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpSession session) {

        Object userId = session.getAttribute("userId");
        Object username = session.getAttribute("username");

        if (userId == null || username == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in first"
            );
        }

        return Map.of(
            "userId", userId,
            "username", username
        );
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpSession session) {

        session.invalidate();

        return Map.of(
            "message",
            "Logged out successfully"
        );
    }
}