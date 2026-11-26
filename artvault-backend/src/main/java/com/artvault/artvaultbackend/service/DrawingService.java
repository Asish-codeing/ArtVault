package com.artvault.artvaultbackend.service;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.artvault.artvaultbackend.entity.Drawing;
import com.artvault.artvaultbackend.repository.DrawingRepository;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class DrawingService {

    private final Cloudinary cloudinary;
    private final DrawingRepository drawingRepository;

    public DrawingService(
            Cloudinary cloudinary,
            DrawingRepository drawingRepository) {

        this.cloudinary = cloudinary;
        this.drawingRepository = drawingRepository;
    }

    public Drawing uploadDrawing(
            String title,
            String description,
            MultipartFile file,
            Long ownerUserId) throws IOException {

        if (ownerUserId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in first."
            );
        }

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                "Please select an image."
            );
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                "Drawing title is required."
            );
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException(
                "Image must be smaller than 10 MB."
            );
        }

        byte[] imageBytes = file.getBytes();

        String detectedType = detectImageType(imageBytes);

        if (detectedType == null) {
            throw new IllegalArgumentException(
                "Invalid image file. Only JPG, PNG and WEBP are allowed."
            );
        }

        String contentType = file.getContentType();

        if (contentType != null &&
                !contentType.equalsIgnoreCase(detectedType)) {

            throw new IllegalArgumentException(
                "Image file type does not match its content."
            );
        }

        Map uploadResult = cloudinary.uploader().upload(
            imageBytes,
            ObjectUtils.asMap(
                "folder", "artvault/drawings",
                "resource_type", "image"
            )
        );

        if (uploadResult.get("secure_url") == null ||
                uploadResult.get("public_id") == null) {

            throw new IllegalStateException(
                "Image upload failed."
            );
        }

        String imageUrl =
            uploadResult.get("secure_url").toString();

        String publicId =
            uploadResult.get("public_id").toString();

        Drawing drawing = new Drawing();

        drawing.setTitle(title.trim());

        drawing.setDescription(
            description == null ? "" : description.trim()
        );

        drawing.setImageUrl(imageUrl);
        drawing.setCloudinaryPublicId(publicId);
        drawing.setOwnerUserId(ownerUserId);

        return drawingRepository.save(drawing);
    }

    // Verify actual image file signature
    private String detectImageType(byte[] bytes) {

        if (bytes == null || bytes.length < 12) {
            return null;
        }

        // JPEG signature
        if ((bytes[0] & 0xFF) == 0xFF &&
                (bytes[1] & 0xFF) == 0xD8 &&
                (bytes[2] & 0xFF) == 0xFF) {

            return "image/jpeg";
        }

        // PNG signature
        if ((bytes[0] & 0xFF) == 0x89 &&
                bytes[1] == 0x50 &&
                bytes[2] == 0x4E &&
                bytes[3] == 0x47 &&
                bytes[4] == 0x0D &&
                bytes[5] == 0x0A &&
                bytes[6] == 0x1A &&
                bytes[7] == 0x0A) {

            return "image/png";
        }

        // WEBP signature
        if (bytes[0] == 'R' &&
                bytes[1] == 'I' &&
                bytes[2] == 'F' &&
                bytes[3] == 'F' &&
                bytes[8] == 'W' &&
                bytes[9] == 'E' &&
                bytes[10] == 'B' &&
                bytes[11] == 'P') {

            return "image/webp";
        }

        return null;
    }

    public void deleteDrawing(
            Long drawingId,
            Long ownerUserId) {

        Drawing drawing = drawingRepository.findById(drawingId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Drawing not found."
            ));

        if (!Objects.equals(
                drawing.getOwnerUserId(),
                ownerUserId)) {

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

            if (!"ok".equals(status) &&
                    !"not found".equals(status)) {

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