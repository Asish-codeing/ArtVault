package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.Comment;
import com.artvault.artvaultbackend.service.CommentService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@CrossOrigin(
    origins = {
        "http://localhost:5500"
    },
    allowCredentials = "true"
)
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    // ADD COMMENT

    @PostMapping("/{drawingId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> addComment(
            @PathVariable Long drawingId,
            @RequestBody CommentRequest request,
            HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in to comment."
            );
        }

        Comment comment = commentService.addComment(
            drawingId,
            (Long) userId,
            request.commentText()
        );

        return Map.of(
            "message", "Comment added successfully!",
            "id", comment.getId(),
            "commentText", comment.getCommentText()
        );
    }

    // GET COMMENTS

    @GetMapping("/{drawingId}")
    public List<CommentResponse> getComments(
            @PathVariable Long drawingId,
            HttpSession session) {

        if (session.getAttribute("userId") == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in to view comments."
            );
        }

        return commentService.getComments(drawingId)
            .stream()
            .map(comment -> new CommentResponse(
                comment.getId(),
                comment.getDrawingId(),
                comment.getUserId(),
                comment.getCommentText(),
                comment.getCreatedAt()
            ))
            .toList();
    }

    public record CommentRequest(String commentText) {
    }

    public record CommentResponse(
        Long id,
        Long drawingId,
        Long userId,
        String commentText,
        java.time.LocalDateTime createdAt
    ) {
    }
}