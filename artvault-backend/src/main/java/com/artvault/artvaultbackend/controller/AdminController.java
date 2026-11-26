package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.repository.UserRepository;
import com.artvault.artvaultbackend.service.AdminAuthorizationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
    origins = "http://localhost:5500",
    allowCredentials = "true"
)
public class AdminController {

    private final UserRepository userRepository;
    private final AdminAuthorizationService adminAuthorizationService;

    public AdminController(
            UserRepository userRepository,
            AdminAuthorizationService adminAuthorizationService) {

        this.userRepository = userRepository;
        this.adminAuthorizationService = adminAuthorizationService;
    }

    // Check admin status
    @GetMapping("/status")
    public Map<String, Boolean> checkAdminStatus(HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        return Map.of("isAdmin", true);
    }

    // View all registered users
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers(HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        return userRepository.findAll()
            .stream()
            .map(user -> Map.<String, Object>of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "createdAt", user.getCreatedAt()
            ))
            .toList();
    }

    // Change a user's role
    @PutMapping("/users/{id}/role")
    public Map<String, String> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        Long currentAdminId = (Long) session.getAttribute("userId");

        if (id.equals(currentAdminId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot change your own role."
            );
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found."
            ));

        String newRole = request.get("role");

        if (newRole == null ||
                (!newRole.equals("USER") && !newRole.equals("ADMIN"))) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Role must be USER or ADMIN."
            );
        }

        user.setRole(newRole);
        userRepository.save(user);

        return Map.of(
            "message", "User role updated successfully!",
            "role", newRole
        );
    }

    // Delete a user account
    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(
            @PathVariable Long id,
            HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        Long currentAdminId = (Long) session.getAttribute("userId");

        if (id.equals(currentAdminId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot delete your own account."
            );
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found."
            ));

        userRepository.delete(user);

        return Map.of(
            "message", "User deleted successfully!"
        );
    }
}