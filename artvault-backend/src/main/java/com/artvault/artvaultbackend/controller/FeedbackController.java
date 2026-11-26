package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.Feedback;
import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.entity.Drawing;

import com.artvault.artvaultbackend.repository.DrawingRepository;
import com.artvault.artvaultbackend.repository.UserRepository;

import com.artvault.artvaultbackend.service.FeedbackService;
import com.artvault.artvaultbackend.service.AdminAuthorizationService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
@CrossOrigin(
    origins = "http://localhost:5500",
    allowCredentials = "true"
)
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final AdminAuthorizationService adminAuthorizationService;
    private final DrawingRepository drawingRepository;
    private final UserRepository userRepository;

    public FeedbackController(
            FeedbackService feedbackService,
            AdminAuthorizationService adminAuthorizationService,
            DrawingRepository drawingRepository,
            UserRepository userRepository) {

        this.feedbackService = feedbackService;
        this.adminAuthorizationService = adminAuthorizationService;
        this.drawingRepository = drawingRepository;
        this.userRepository = userRepository;
    }

    // ADMIN: Create or update private feedback
    @PostMapping("/{drawingId}")
    public Map<String, Object> saveFeedback(
            @PathVariable Long drawingId,
            @RequestBody FeedbackRequest request,
            HttpSession session) {

        User admin = adminAuthorizationService.requireAdmin(session);

        Feedback feedback = feedbackService.saveFeedback(
            drawingId,
            admin.getId(),
            request.feedbackText()
        );

        return Map.of(
            "message", "Private feedback saved successfully!",
            "feedbackId", feedback.getId()
        );
    }

    // OWNER OR ADMIN: Read private feedback
    @GetMapping("/{drawingId}")
    public FeedbackResponse getFeedback(
            @PathVariable Long drawingId,
            HttpSession session) {

        Object sessionUserId = session.getAttribute("userId");

        if (!(sessionUserId instanceof Long)) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in first."
            );
        }

        User user = userRepository.findById((Long) sessionUserId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User account not found."
            ));

        Drawing drawing = drawingRepository.findById(drawingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            ));

        boolean isAdmin = "ADMIN".equals(user.getRole());
        boolean isOwner = drawing.getOwnerUserId().equals(user.getId());

        if (!isAdmin && !isOwner) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You cannot view this private feedback."
            );
        }

        Feedback feedback = feedbackService.getFeedback(drawingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No feedback available for this drawing."
            ));

        return new FeedbackResponse(
            feedback.getId(),
            feedback.getDrawingId(),
            feedback.getFeedbackText(),
            feedback.getCreatedAt(),
            feedback.getUpdatedAt()
        );
    }

    // ADMIN: Delete private feedback
    @DeleteMapping("/{drawingId}")
    public Map<String, String> deleteFeedback(
            @PathVariable Long drawingId,
            HttpSession session) {

        adminAuthorizationService.requireAdmin(session);

        feedbackService.deleteFeedback(drawingId);

        return Map.of(
            "message", "Private feedback deleted successfully!"
        );
    }

    public record FeedbackRequest(String feedbackText) {}

    public record FeedbackResponse(
        Long id,
        Long drawingId,
        String feedbackText,
        java.time.LocalDateTime createdAt,
        java.time.LocalDateTime updatedAt
    ) {}
}