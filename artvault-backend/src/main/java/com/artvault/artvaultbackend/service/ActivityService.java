package com.artvault.artvaultbackend.service;

import com.artvault.artvaultbackend.entity.Activity;
import com.artvault.artvaultbackend.repository.ActivityRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    // Save a new activity
    public void logActivity(
            Long userId,
            String username,
            String action,
            String description) {

        Activity activity = new Activity();

        activity.setUserId(userId);
        activity.setUsername(username);
        activity.setAction(action);
        activity.setDescription(description);

        activityRepository.save(activity);
    }

    // Get all activities, newest first
    public List<Activity> getAllActivities() {

        return activityRepository.findAllByOrderByCreatedAtDesc();
    }

    // Get activities of a particular user
    public List<Activity> getUserActivities(Long userId) {

        return activityRepository.findByUserId(userId);
    }
}