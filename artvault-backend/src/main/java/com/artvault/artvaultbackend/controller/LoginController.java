package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.repository.UserRepository;
import com.artvault.artvaultbackend.service.ActivityService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
    origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "https://9hlzj8hp-5500.inc1.devtunnels.ms"
    },
    allowCredentials = "true"
)
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ActivityService activityService;

    public LoginController(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ActivityService activityService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityService = activityService;
    }

    @PostMapping("/login")
    public Map<String, Object> login(
            @RequestBody LoginRequest request,
            HttpSession session) {

        String identity = request.identity().trim();

        User user = userRepository
                .findByUsernameIgnoreCaseOrEmailIgnoreCase(
                    identity, identity
                )
                .orElse(null);

        if (user == null ||
                !passwordEncoder.matches(
                    request.password(),
                    user.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username/email or password"
            );
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("username", user.getUsername());

        activityService.logActivity(
                user.getId(),
                user.getUsername(),
                "USER_LOGIN",
                "User logged in successfully"
        );

        return Map.of(
                "message", "Login successful!",
                "username", user.getUsername(),
                "role", user.getRole()
        );
    }

    public record LoginRequest(
            String identity,
            String password) {
    }
}