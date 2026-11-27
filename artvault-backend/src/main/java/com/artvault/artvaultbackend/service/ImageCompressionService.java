
package com.artvault.artvaultbackend.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ImageCompressionService {

    private static final int TARGET_SIZE = 500 * 1024;

    private static final int MAX_WIDTH = 2500;
    private static final int MAX_HEIGHT = 2500;

    private static final long MAX_PIXELS = 40_000_000L;

    public byte[] compressImage(byte[] originalBytes)
            throws IOException {

        if (originalBytes == null || originalBytes.length == 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Image is empty."
            );
        }

        BufferedImage originalImage;

        try (ByteArrayInputStream input =
                    new ByteArrayInputStream(originalBytes);
             ImageInputStream imageInput =
                    ImageIO.createImageInputStream(input)) {

            if (imageInput == null) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unable to read image."
                );
            }

            Iterator<ImageReader> readers =
                ImageIO.getImageReaders(imageInput);

            if (!readers.hasNext()) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported image format."
                );
            }

            ImageReader reader = readers.next();

            try {
                reader.setInput(imageInput, true, true);

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0 ||
                        width > MAX_WIDTH * 4 ||
                        height > MAX_HEIGHT * 4 ||
                        (long) width * height > MAX_PIXELS) {

                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Image dimensions are too large."
                    );
                }

                originalImage = reader.read(0);

            } finally {
                reader.dispose();
            }
        }

        if (originalImage == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid image."
            );
        }

        BufferedImage resizedImage = resizeImage(originalImage);

        float[] qualityLevels = {
            0.90f,
            0.85f,
            0.80f,
            0.75f,
            0.70f,
            0.65f,
            0.60f,
            0.55f
        };

        byte[] bestResult = null;

        for (int attempt = 0; attempt < 5; attempt++) {

            for (float quality : qualityLevels) {

                byte[] compressed = encodeWebP(
                    resizedImage,
                    quality
                );

                if (bestResult == null ||
                        compressed.length < bestResult.length) {

                    bestResult = compressed;
                }

                if (compressed.length <= TARGET_SIZE) {
                    return compressed;
                }
            }

            if (resizedImage.getWidth() <= 500 ||
                    resizedImage.getHeight() <= 500) {
                break;
            }

            resizedImage = Thumbnails.of(resizedImage)
                .scale(0.80)
                .asBufferedImage();
        }

        return bestResult;
    }

    private BufferedImage resizeImage(
            BufferedImage originalImage) throws IOException {

        return Thumbnails.of(originalImage)
            .size(MAX_WIDTH, MAX_HEIGHT)
            .keepAspectRatio(true)
            .asBufferedImage();
    }

    private byte[] encodeWebP(
            BufferedImage image,
            float quality) throws IOException {

        Iterator<ImageWriter> writers =
            ImageIO.getImageWritersByMIMEType("image/webp");

        if (!writers.hasNext()) {
            throw new IllegalStateException(
                "WebP encoder is unavailable."
            );
        }

        ImageWriter writer = writers.next();

        try (ByteArrayOutputStream output =
                    new ByteArrayOutputStream()) {

            try (ImageOutputStream imageOutput =
                        ImageIO.createImageOutputStream(output)) {

                writer.setOutput(imageOutput);

                ImageWriteParam parameters =
                    writer.getDefaultWriteParam();

                if (parameters.canWriteCompressed()) {

                    parameters.setCompressionMode(
                        ImageWriteParam.MODE_EXPLICIT
                    );

                    String[] compressionTypes =
                        parameters.getCompressionTypes();

                    if (compressionTypes != null &&
                            compressionTypes.length > 0) {

                        parameters.setCompressionType(
                            compressionTypes[0]
                        );
                    }

                    parameters.setCompressionQuality(quality);
                }

                writer.write(
                    null,
                    new IIOImage(image, null, null),
                    parameters
                );

                imageOutput.flush();
            }

            return output.toByteArray();

        } finally {
            writer.dispose();
        }
    }
}
