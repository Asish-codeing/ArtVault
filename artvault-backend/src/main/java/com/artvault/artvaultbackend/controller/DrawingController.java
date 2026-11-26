package com.artvault.artvaultbackend.controller;

import com.artvault.artvaultbackend.entity.Drawing;
import com.artvault.artvaultbackend.entity.User;
import com.artvault.artvaultbackend.repository.DrawingRepository;
import com.artvault.artvaultbackend.repository.UserRepository;
import com.artvault.artvaultbackend.service.DrawingService;
import com.artvault.artvaultbackend.service.ActivityService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/drawings")
@CrossOrigin(
    origins = "http://localhost:5500",
    allowCredentials = "true"
)
public class DrawingController {

    private final DrawingService drawingService;
    private final DrawingRepository drawingRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    public DrawingController(
            DrawingService drawingService,
            DrawingRepository drawingRepository,
            UserRepository userRepository,
            ActivityService activityService) {

        this.drawingService = drawingService;
        this.drawingRepository = drawingRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    // UPLOAD DRAWING

    @PostMapping(
        value = "/upload",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> uploadDrawing(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file,
            HttpSession session) throws IOException {

        Object userId = session.getAttribute("userId");

        if (userId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in before uploading."
            );
        }

        Long loggedInUserId = (Long) userId;

        Drawing drawing = drawingService.uploadDrawing(
            title,
            description,
            file,
            loggedInUserId
        );

        User user = userRepository.findById(loggedInUserId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User account not found."
            ));

        // Record drawing upload
        activityService.logActivity(
            user.getId(),
            user.getUsername(),
            "DRAWING_UPLOADED",
            "Uploaded drawing: " + drawing.getTitle()
        );

        return Map.of(
            "message", "Drawing uploaded successfully!",
            "id", drawing.getId(),
            "title", drawing.getTitle(),
            "imageUrl", drawing.getImageUrl()
        );
    }

    // DELETE DRAWING
    // OWNER CAN DELETE OWN DRAWING
    // ADMIN CAN DELETE ANY DRAWING

    @DeleteMapping("/{id}")
    public Map<String, String> deleteDrawing(
            @PathVariable Long id,
            HttpSession session) {

        Object userIdObject = session.getAttribute("userId");

        if (userIdObject == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in before deleting."
            );
        }

        Long userId = (Long) userIdObject;

        User loggedInUser = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User account not found."
            ));

        Drawing drawing = drawingRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            ));

        String drawingTitle = drawing.getTitle();

        if ("ADMIN".equals(loggedInUser.getRole())) {

            drawingService.deleteDrawing(
                id,
                drawing.getOwnerUserId()
            );

        } else {

            drawingService.deleteDrawing(id, userId);

        }

        // Record drawing deletion by the person performing it
        activityService.logActivity(
            loggedInUser.getId(),
            loggedInUser.getUsername(),
            "DRAWING_DELETED",
            "Deleted drawing: " + drawingTitle
        );

        return Map.of(
            "message",
            "Drawing deleted successfully!"
        );
    }

    // GET ALL DRAWINGS

    @GetMapping
    public List<DrawingResponse> getAllDrawings(HttpSession session) {

        if (session.getAttribute("userId") == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in to view drawings."
            );
        }

        return drawingRepository.findAllByOrderByUploadedAtDesc()
            .stream()
            .map(drawing -> {

                String artistUsername = userRepository
                    .findById(drawing.getOwnerUserId())
                    .map(User::getUsername)
                    .orElse("Unknown Artist");

                return new DrawingResponse(
                    drawing.getId(),
                    drawing.getTitle(),
                    drawing.getDescription(),
                    drawing.getImageUrl(),
                    drawing.getOwnerUserId(),
                    artistUsername,
                    drawing.getUploadedAt()
                );
            })
            .toList();
    }

    // DRAWING RESPONSE

    public record DrawingResponse(
        Long id,
        String title,
        String description,
        String imageUrl,
        Long ownerUserId,
        String artistUsername,
        LocalDateTime uploadedAt
    ) {
    }

}