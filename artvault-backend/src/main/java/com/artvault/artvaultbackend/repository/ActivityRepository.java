package com.artvault.artvaultbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.artvault.artvaultbackend.entity.Activity;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Get activities belonging to a user
    List<Activity> findByUserIdOrderByCreatedAtDesc(Long userId);

    // Get all activities, newest first
    List<Activity> findAllByOrderByCreatedAtDesc();

    // Find activities by username
    List<Activity> findByUsernameIgnoreCase(String username);

    // Delete all activities belonging to a user
    void deleteByUserId(Long userId);
}