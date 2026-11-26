package com.artvault.artvaultbackend.service;

import com.artvault.artvaultbackend.entity.Activity;
import com.artvault.artvaultbackend.repository.ActivityRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(
            ActivityRepository activityRepository) {

        this.activityRepository = activityRepository;
    }

    // Save a new activity
    public void logActivity(
            Long userId,
            String username,
            String action,
            String description) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                "Valid user ID is required."
            );
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException(
                "Username is required."
            );
        }

        if (username.trim().length() > 30) {
            throw new IllegalArgumentException(
                "Username is too long."
            );
        }

        if (action == null || action.isBlank()) {
            throw new IllegalArgumentException(
                "Activity action is required."
            );
        }

        if (action.trim().length() > 50) {
            throw new IllegalArgumentException(
                "Activity action is too long."
            );
        }

        if (description != null &&
                description.length() > 500) {

            throw new IllegalArgumentException(
                "Activity description cannot exceed 500 characters."
            );
        }

        Activity activity = new Activity();

        activity.setUserId(userId);
        activity.setUsername(username.trim());
        activity.setAction(action.trim());

        activity.setDescription(
            description == null ? "" : description.trim()
        );

        activityRepository.save(activity);
    }

    // Get all activities, newest first
    public List<Activity> getAllActivities() {

        return activityRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get activities of a particular user, newest first
    public List<Activity> getUserActivities(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                "Valid user ID is required."
            );
        }

        return activityRepository
            .findByUserIdOrderByCreatedAtDesc(userId);
    }
}