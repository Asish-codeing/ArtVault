package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.repository.UserRepository;
import com.artvault.artvaultbackend.repository.ActivityRepository;
import com.artvault.artvaultbackend.service.AdminAuthorizationService;
import com.artvault.artvaultbackend.service.DrawingService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(
    origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "https://artvault-gallery.netlify.app"
    },
    allowCredentials = "true"
)
public class AdminController {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final AdminAuthorizationService adminAuthorizationService;
    private final DrawingService drawingService;

    public AdminController(
            UserRepository userRepository,
            ActivityRepository activityRepository,
            AdminAuthorizationService adminAuthorizationService,
            DrawingService drawingService) {

        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.adminAuthorizationService = adminAuthorizationService;
        this.drawingService = drawingService;
    }

    // Check admin access
    @GetMapping("/status")
    public Map<String, Boolean> checkAdminStatus(
            HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        return Map.of("isAdmin", true);
    }

    // Get all users
    @GetMapping("/users")
    public List<Map<String, Object>> getAllUsers(
            HttpSession session) {

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

    // Update user role
    @PutMapping("/users/{id}/role")
    public Map<String, String> updateUserRole(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request,
            HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        Long currentAdminId =
            (Long) session.getAttribute("userId");

        if (id.equals(currentAdminId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "You cannot change your own role."
            );
        }

        if (request == null ||
                request.get("role") == null) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Role is required."
            );
        }

        String newRole = request.get("role").trim();

        if (!newRole.equals("USER") &&
                !newRole.equals("ADMIN")) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Role must be USER or ADMIN."
            );
        }

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "User not found."
            ));

        // Prevent demoting the last administrator
        if ("ADMIN".equals(user.getRole()) &&
                "USER".equals(newRole)) {

            long adminCount =
                userRepository.countByRole("ADMIN");

            if (adminCount <= 1) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot remove the last administrator."
                );
            }
        }

        user.setRole(newRole);
        userRepository.save(user);

        return Map.of(
            "message", "User role updated successfully!",
            "role", newRole
        );
    }

    // Delete user and associated data
    @DeleteMapping("/users/{id}")
    public Map<String, String> deleteUser(
            @PathVariable Long id,
            HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        Long currentAdminId =
            (Long) session.getAttribute("userId");

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

        // Prevent deleting another administrator
        if ("ADMIN".equals(user.getRole())) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You cannot delete another administrator."
            );
        }

        // Step 1: Delete drawings and Cloudinary images
        drawingService.deleteAllUserDrawings(user.getId());

        // Step 2: Delete activity records
        activityRepository.deleteByUserId(user.getId());

        // Step 3: Delete user account
        userRepository.delete(user);

        return Map.of(
            "message",
            "User and all associated data deleted successfully!"
        );
    }
}