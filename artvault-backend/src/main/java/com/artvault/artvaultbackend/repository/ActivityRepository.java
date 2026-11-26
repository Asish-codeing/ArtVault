package com.artvault.artvaultbackend.repository;

import com.artvault.artvaultbackend.entity.Activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    // Find activities of a particular user
    List<Activity> findByUserId(Long userId);

    // Display latest activities first
    List<Activity> findAllByOrderByCreatedAtDesc();

    // Find activities by username
    List<Activity> findByUsernameIgnoreCase(String username);
}