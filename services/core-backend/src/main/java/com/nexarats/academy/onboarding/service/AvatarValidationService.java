package com.nexarats.academy.onboarding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

@Service
public class AvatarValidationService {

    private static final Logger log = LoggerFactory.getLogger(AvatarValidationService.class);

    public static final long MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB
    public static final int MAX_DIMENSION = 4096;

    // Magic bytes signatures
    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = new byte[]{0x52, 0x49, 0x46, 0x46}; // 'R', 'I', 'F', 'F'
    private static final byte[] WEBP_MAGIC = new byte[]{0x57, 0x45, 0x42, 0x50}; // 'W', 'E', 'B', 'P'

    public record ValidatedAvatar(
            String detectedContentType,
            String fileExtension,
            byte[] fileBytes,
            int width,
            int height
    ) {
    }

    public ValidatedAvatar validateAvatarFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file cannot be empty.");
        }

        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            log.warn("Uploaded file exceeds 5MB limit: {} bytes", file.getSize());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file exceeds maximum allowed size of 5 MB.");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            log.error("Failed to read uploaded avatar file bytes", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Could not read avatar file.");
        }

        // Magic byte verification
        String detectedType = detectImageMimeType(bytes);
        if (detectedType == null) {
            log.warn("Uploaded file rejected: magic bytes do not match supported image formats (JPEG, PNG, WebP).");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file type. Supported formats are JPEG, PNG, and WebP. SVG and executable formats are prohibited."
            );
        }

        // Validate image dimensions and integrity via ImageIO (prevent decompression bombs or truncated streams)
        int width;
        int height;
        try (InputStream is = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(is);
            if (image == null && !"image/webp".equals(detectedType)) {
                // ImageIO natively handles PNG and JPEG; standard Java 21 ImageIO might not parse WebP without plugin
                log.warn("ImageIO failed to decode image data for format {}", detectedType);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corrupted or invalid image data.");
            }
            if (image != null) {
                width = image.getWidth();
                height = image.getHeight();
                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION) {
                    log.warn("Image dimensions out of bounds: {}x{}", width, height);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image dimensions must be between 1x1 and 4096x4096 pixels.");
                }
            } else {
                // For WebP without custom ImageReader, default to valid dimensions
                width = 512;
                height = 512;
            }
        } catch (IOException e) {
            log.error("Failed to verify image dimensions", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corrupted image file.");
        }

        String extension = switch (detectedType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "img";
        };

        return new ValidatedAvatar(detectedType, extension, bytes, width, height);
    }

    public String detectImageMimeType(byte[] bytes) {
        if (bytes == null || bytes.length < 12) {
            return null;
        }

        // Check JPEG (0xFF, 0xD8, 0xFF)
        if ((bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }

        // Check PNG (8-byte header: 89 50 4E 47 0D 0A 1A 0A)
        if (bytes.length >= 8 && Arrays.equals(Arrays.copyOfRange(bytes, 0, 8), PNG_MAGIC)) {
            return "image/png";
        }

        // Check WebP (RIFF....WEBP)
        if (bytes.length >= 12 &&
                Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), RIFF_MAGIC) &&
                Arrays.equals(Arrays.copyOfRange(bytes, 8, 12), WEBP_MAGIC)) {
            return "image/webp";
        }

        return null;
    }
}
