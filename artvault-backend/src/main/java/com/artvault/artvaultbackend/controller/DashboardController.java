package com.artvault.artvaultbackend.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
        "http://127.0.0.1:5500",
        "https://artvault-gallery.netlify.app"
    },
    allowCredentials = "true"
)
public class DashboardController {

    // GET CURRENT USER
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpSession session) {

        Object userId = session.getAttribute("userId");
        Object username = session.getAttribute("username");

        if (!(userId instanceof Long) ||
                !(username instanceof String)) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in first."
            );
        }

        return Map.of(
            "userId", userId,
            "username", username
        );
    }

    // LOGOUT
    @PostMapping("/logout")
    public Map<String, String> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Find existing session without creating a new one
        HttpSession session = request.getSession(false);

        // Destroy server-side session
        if (session != null) {
            session.invalidate();
        }

        // Expire browser session cookie
        response.setHeader(
            "Set-Cookie",
            "JSESSIONID=; Path=/; Max-Age=0; HttpOnly; Secure; SameSite=None"
        );

        return Map.of(
            "message",
            "Logged out successfully"
        );
    }
}