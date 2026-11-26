package com.artvault.artvaultbackend.service;

import com.artvault.artvaultbackend.entity.Comment;
import com.artvault.artvaultbackend.repository.CommentRepository;
import com.artvault.artvaultbackend.repository.DrawingRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final DrawingRepository drawingRepository;

    public CommentService(
            CommentRepository commentRepository,
            DrawingRepository drawingRepository) {

        this.commentRepository = commentRepository;
        this.drawingRepository = drawingRepository;
    }

    // ADD A COMMENT

    public Comment addComment(
            Long drawingId,
            Long userId,
            String commentText) {

        if (drawingId == null || !drawingRepository.existsById(drawingId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            );
        }

        if (commentText == null || commentText.isBlank()) {
            throw new IllegalArgumentException(
                "Comment cannot be empty."
            );
        }

        if (commentText.length() > 1000) {
            throw new IllegalArgumentException(
                "Comment cannot exceed 1000 characters."
            );
        }

        Comment comment = new Comment();

        comment.setDrawingId(drawingId);
        comment.setUserId(userId);
        comment.setCommentText(commentText.trim());

        return commentRepository.save(comment);
    }

    // GET COMMENTS FOR A DRAWING

    public List<Comment> getComments(Long drawingId) {

        if (drawingId == null || !drawingRepository.existsById(drawingId)) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            );
        }

        return commentRepository
            .findByDrawingIdOrderByCreatedAtAsc(drawingId);
    }
}