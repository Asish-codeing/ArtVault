package com.artvault.artvaultbackend.repository;

import com.artvault.artvaultbackend.entity.Drawing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DrawingRepository extends JpaRepository<Drawing, Long> {

    List<Drawing> findAllByOrderByUploadedAtDesc();

    List<Drawing> findByOwnerUserIdOrderByUploadedAtDesc(Long ownerUserId);
}