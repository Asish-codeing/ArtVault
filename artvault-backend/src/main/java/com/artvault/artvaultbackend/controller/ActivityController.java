package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.Activity;
import com.artvault.artvaultbackend.service.ActivityService;
import com.artvault.artvaultbackend.service.AdminAuthorizationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/activities")
@CrossOrigin(
    origins = {
        "http://localhost:5500",
        "http://127.0.0.1:5500",
        "https://artvault-gallery.netlify.app"
    },
    allowCredentials = "true"
)
public class ActivityController {

    private final ActivityService activityService;
    private final AdminAuthorizationService adminAuthorizationService;

    public ActivityController(
            ActivityService activityService,
            AdminAuthorizationService adminAuthorizationService) {

        this.activityService = activityService;
        this.adminAuthorizationService = adminAuthorizationService;
    }

    // Only administrators can view all activities
    @GetMapping
    public List<Activity> getAllActivities(HttpSession session) {

        // Verify administrator permissions
        adminAuthorizationService.requireAdmin(session);

        // Return activity records, newest first
        return activityService.getAllActivities();
    }
}