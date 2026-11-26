package com.artvault.artvaultbackend.repository;

import com.artvault.artvaultbackend.entity.Feedback;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // Find feedback for a particular drawing
    Optional<Feedback> findByDrawingId(Long drawingId);

    // Find all feedback written by a particular admin
    List<Feedback> findByAdminUserId(Long adminUserId);

    // Delete feedback belonging to a particular drawing
    void deleteByDrawingId(Long drawingId);
}