package com.artvault.artvaultbackend.service;

import com.artvault.artvaultbackend.entity.Feedback;
import com.artvault.artvaultbackend.repository.FeedbackRepository;
import com.artvault.artvaultbackend.repository.DrawingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final DrawingRepository drawingRepository;

    public FeedbackService(
            FeedbackRepository feedbackRepository,
            DrawingRepository drawingRepository) {

        this.feedbackRepository = feedbackRepository;
        this.drawingRepository = drawingRepository;
    }

    // Create or update feedback for a drawing
    public Feedback saveFeedback(
            Long drawingId,
            Long adminUserId,
            String feedbackText) {

        if (drawingId == null || !drawingRepository.existsById(drawingId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            );
        }

        if (feedbackText == null || feedbackText.isBlank()) {
            throw new IllegalArgumentException(
                "Feedback cannot be empty."
            );
        }

        if (feedbackText.length() > 2000) {
            throw new IllegalArgumentException(
                "Feedback cannot exceed 2000 characters."
            );
        }

        Optional<Feedback> existingFeedback =
                feedbackRepository.findByDrawingId(drawingId);

        Feedback feedback = existingFeedback.orElseGet(Feedback::new);

        feedback.setDrawingId(drawingId);
        feedback.setAdminUserId(adminUserId);
        feedback.setFeedbackText(feedbackText.trim());
        feedback.setUpdatedAt(LocalDateTime.now());

        return feedbackRepository.save(feedback);
    }

    // Get feedback for a drawing
    public Optional<Feedback> getFeedback(Long drawingId) {

        return feedbackRepository.findByDrawingId(drawingId);
    }

    // Delete feedback
    public void deleteFeedback(Long drawingId) {

        feedbackRepository.deleteByDrawingId(drawingId);
    }
}