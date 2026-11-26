package com.artvault.artvaultbackend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "artvault_comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long drawingId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 1000)
    private String commentText;

    private LocalDateTime createdAt = LocalDateTime.now();

    public Comment() {
    }

    public Long getId() {
        return id;
    }

    public Long getDrawingId() {
        return drawingId;
    }

    public void setDrawingId(Long drawingId) {
        this.drawingId = drawingId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}