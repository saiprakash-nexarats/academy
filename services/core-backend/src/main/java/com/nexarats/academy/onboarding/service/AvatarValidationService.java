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
    public static final int MIN_DIMENSION = 32;
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

        return validateAvatarBytes(bytes, file.getOriginalFilename());
    }

    public ValidatedAvatar validateAvatarBytes(byte[] bytes, String filename) {
        if (bytes == null || bytes.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file cannot be empty.");
        }

        if (bytes.length > MAX_FILE_SIZE_BYTES) {
            log.warn("Uploaded file exceeds 5MB limit: {} bytes", bytes.length);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Avatar file exceeds maximum allowed size of 5 MB.");
        }

        // 1. Magic byte verification
        String detectedType = detectImageMimeType(bytes);
        if (detectedType == null) {
            log.warn("Uploaded file rejected: magic bytes do not match supported image formats (JPEG, PNG, WebP).");
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid file type. Supported formats are JPEG, PNG, and WebP. SVG and executable formats are prohibited."
            );
        }

        // 2. Exact dimension and integrity verification (never assign placeholder dimensions)
        int width;
        int height;

        if ("image/webp".equals(detectedType)) {
            Dimension dim = extractWebpDimensions(bytes);
            width = dim.width();
            height = dim.height();
        } else {
            try (InputStream is = new ByteArrayInputStream(bytes)) {
                BufferedImage image = ImageIO.read(is);
                if (image == null) {
                    log.warn("ImageIO failed to decode image data for format {}", detectedType);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corrupted or invalid image data.");
                }
                width = image.getWidth();
                height = image.getHeight();
            } catch (IOException e) {
                log.error("Failed to decode image data: {}", e.getMessage());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Corrupted or invalid image data.");
            }
        }

        // Check dimension bounds
        if (width < MIN_DIMENSION || height < MIN_DIMENSION || width > MAX_DIMENSION || height > MAX_DIMENSION) {
            log.warn("Image dimensions out of bounds: {}x{} (allowed: {}x{} to {}x{})",
                    width, height, MIN_DIMENSION, MIN_DIMENSION, MAX_DIMENSION, MAX_DIMENSION);
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    String.format("Image dimensions %dx%d out of bounds. Allowed range: %dx%d to %dx%d pixels.",
                            width, height, MIN_DIMENSION, MIN_DIMENSION, MAX_DIMENSION, MAX_DIMENSION)
            );
        }

        String extension = switch (detectedType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "img";
        };

        return new ValidatedAvatar(detectedType, extension, bytes, width, height);
    }

    public record Dimension(int width, int height) {}

    /**
     * Extracts actual pixel dimensions from standard WebP container chunks (VP8, VP8L, VP8X).
     * Never assigns placeholder or fabricated dimensions.
     */
    public Dimension extractWebpDimensions(byte[] bytes) {
        if (bytes == null || bytes.length < 30) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed WebP image header: header too short.");
        }

        // Verify RIFF header and WEBP signature
        if (!Arrays.equals(Arrays.copyOfRange(bytes, 0, 4), RIFF_MAGIC) ||
                !Arrays.equals(Arrays.copyOfRange(bytes, 8, 12), WEBP_MAGIC)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid WebP header.");
        }

        String chunkFourCc = new String(bytes, 12, 4);

        if ("VP8 ".equals(chunkFourCc)) {
            // Lossy VP8 bitstream chunk
            if (bytes.length < 30) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Truncated VP8 WebP image chunk.");
            }
            // Check start code at bytes 23..25: 0x9D 0x01 0x2A
            if ((bytes[23] & 0xFF) != 0x9D || (bytes[24] & 0xFF) != 0x01 || (bytes[25] & 0xFF) != 0x2A) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VP8 start code in WebP image.");
            }
            int width = ((bytes[26] & 0xFF) | ((bytes[27] & 0xFF) << 8)) & 0x3FFF;
            int height = ((bytes[28] & 0xFF) | ((bytes[29] & 0xFF) << 8)) & 0x3FFF;
            return new Dimension(width, height);
        } else if ("VP8L".equals(chunkFourCc)) {
            // Lossless VP8L bitstream chunk
            if (bytes.length < 25) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Truncated VP8L WebP image chunk.");
            }
            // Check signature byte at offset 20: 0x2F
            if ((bytes[20] & 0xFF) != 0x2F) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid VP8L signature byte in WebP image.");
            }
            int b0 = bytes[21] & 0xFF;
            int b1 = bytes[22] & 0xFF;
            int b2 = bytes[23] & 0xFF;
            int b3 = bytes[24] & 0xFF;

            int width = 1 + (((b1 & 0x3F) << 8) | b0);
            int height = 1 + ((((b3 & 0x0F) << 10) | (b2 << 2) | ((b1 & 0xC0) >> 6)));
            return new Dimension(width, height);
        } else if ("VP8X".equals(chunkFourCc)) {
            // Extended VP8X chunk
            if (bytes.length < 30) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Truncated VP8X WebP image chunk.");
            }
            int width = 1 + ((bytes[24] & 0xFF) | ((bytes[25] & 0xFF) << 8) | ((bytes[26] & 0xFF) << 16));
            int height = 1 + ((bytes[27] & 0xFF) | ((bytes[28] & 0xFF) << 8) | ((bytes[29] & 0xFF) << 16));
            return new Dimension(width, height);
        } else {
            log.warn("Unsupported WebP chunk type: {}", chunkFourCc);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported WebP chunk type: " + chunkFourCc);
        }
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
