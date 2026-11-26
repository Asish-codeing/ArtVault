package com.artvault.artvaultbackend.repository;

import com.artvault.artvaultbackend.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByDrawingIdOrderByCreatedAtAsc(Long drawingId);

    List<Comment> findByUserIdOrderByCreatedAtDesc(Long userId);

}