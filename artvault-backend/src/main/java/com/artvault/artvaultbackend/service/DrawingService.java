package com.artvault.artvaultbackend.service;

import com.artvault.artvaultbackend.entity.Drawing;
import com.artvault.artvaultbackend.repository.DrawingRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@Service
public class DrawingService {

    private final Cloudinary cloudinary;
    private final DrawingRepository drawingRepository;

    public DrawingService(Cloudinary cloudinary, DrawingRepository drawingRepository) {
        this.cloudinary = cloudinary;
        this.drawingRepository = drawingRepository;
    }

    public Drawing uploadDrawing(
            String title,
            String description,
            MultipartFile file,
            Long ownerUserId) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image.");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Drawing title is required.");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("Image must be smaller than 10 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg") ||
                  contentType.equals("image/png") ||
                  contentType.equals("image/webp"))) {
            throw new IllegalArgumentException(
                "Only JPG, PNG and WEBP images are allowed."
            );
        }

        Map uploadResult = cloudinary.uploader().upload(
            file.getBytes(),
            ObjectUtils.asMap(
                "folder", "artvault/drawings",
                "resource_type", "image"
            )
        );

        String imageUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        Drawing drawing = new Drawing();
        drawing.setTitle(title.trim());
        drawing.setDescription(description == null ? "" : description.trim());
        drawing.setImageUrl(imageUrl);
        drawing.setCloudinaryPublicId(publicId);
        drawing.setOwnerUserId(ownerUserId);

        return drawingRepository.save(drawing);
    }

    public void deleteDrawing(Long drawingId, Long ownerUserId) {

        Drawing drawing = drawingRepository.findById(drawingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            ));

        if (!drawing.getOwnerUserId().equals(ownerUserId)) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "You cannot delete another user's drawing."
            );
        }

        try {
            Map result = cloudinary.uploader().destroy(
                drawing.getCloudinaryPublicId(),
                ObjectUtils.asMap(
                    "resource_type", "image",
                    "invalidate", true
                )
            );

            Object status = result.get("result");

            if (!"ok".equals(status) && !"not found".equals(status)) {
                throw new IllegalStateException(
                    "Cloudinary could not delete the image."
                );
            }

            drawingRepository.delete(drawing);

        } catch (Exception e) {
            throw new IllegalStateException(
                "Unable to delete artwork. Please try again.",
                e
            );
        }
    }
}