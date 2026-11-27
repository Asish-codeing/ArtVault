package com.artvault.artvaultbackend.service;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class ImageCompressionServiceTest {

    @Test
    void shouldCompressImageIntoWebP() throws Exception {

        // Read the sample image from the backend folder
        byte[] original = Files.readAllBytes(
            Path.of("test-image.jpg")
        );

        ImageCompressionService service =
            new ImageCompressionService();

        // Compress the image
        byte[] compressed =
            service.compressImage(original);

        // Check that output is not empty
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);

        // Check WebP file signature
        assertEquals('R', compressed[0]);
        assertEquals('I', compressed[1]);
        assertEquals('F', compressed[2]);
        assertEquals('F', compressed[3]);

        assertEquals('W', compressed[8]);
        assertEquals('E', compressed[9]);
        assertEquals('B', compressed[10]);
        assertEquals('P', compressed[11]);

        System.out.println(
            "Original size: " +
            original.length / 1024 + " KB"
        );

        System.out.println(
            "Compressed size: " +
            compressed.length / 1024 + " KB"
        );

        System.out.println(
            "Compression test successful!"
        );
    }
}