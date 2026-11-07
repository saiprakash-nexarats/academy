package com.nexarats.academy.onboarding;

import com.nexarats.academy.onboarding.service.AvatarValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AvatarValidationServiceTest {

    private AvatarValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new AvatarValidationService();
    }

    private byte[] createTestImageBytes(String format, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("Successfully validates legitimate JPEG image")
    void validatesValidJpeg() throws IOException {
        byte[] jpegBytes = createTestImageBytes("jpg", 200, 200);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.jpg", "image/jpeg", jpegBytes);

        AvatarValidationService.ValidatedAvatar result = validationService.validateAvatarFile(file);

        assertThat(result).isNotNull();
        assertThat(result.detectedContentType()).isEqualTo("image/jpeg");
        assertThat(result.fileExtension()).isEqualTo("jpg");
        assertThat(result.width()).isEqualTo(200);
        assertThat(result.height()).isEqualTo(200);
    }

    @Test
    @DisplayName("Successfully validates legitimate PNG image")
    void validatesValidPng() throws IOException {
        byte[] pngBytes = createTestImageBytes("png", 150, 150);
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", pngBytes);

        AvatarValidationService.ValidatedAvatar result = validationService.validateAvatarFile(file);

        assertThat(result).isNotNull();
        assertThat(result.detectedContentType()).isEqualTo("image/png");
        assertThat(result.fileExtension()).isEqualTo("png");
        assertThat(result.width()).isEqualTo(150);
        assertThat(result.height()).isEqualTo(150);
    }

    @Test
    @DisplayName("Successfully validates WebP header")
    void validatesValidWebPHeader() {
        byte[] webpBytes = new byte[32];
        // 'RIFF'
        webpBytes[0] = 'R'; webpBytes[1] = 'I'; webpBytes[2] = 'F'; webpBytes[3] = 'F';
        // length
        webpBytes[4] = 24; webpBytes[5] = 0; webpBytes[6] = 0; webpBytes[7] = 0;
        // 'WEBP'
        webpBytes[8] = 'W'; webpBytes[9] = 'E'; webpBytes[10] = 'B'; webpBytes[11] = 'P';

        MockMultipartFile file = new MockMultipartFile("file", "avatar.webp", "image/webp", webpBytes);

        AvatarValidationService.ValidatedAvatar result = validationService.validateAvatarFile(file);

        assertThat(result).isNotNull();
        assertThat(result.detectedContentType()).isEqualTo("image/webp");
        assertThat(result.fileExtension()).isEqualTo("webp");
    }

    @Test
    @DisplayName("Rejects SVG files with XML/HTML payloads")
    void rejectsSvgFiles() {
        byte[] svgBytes = "<svg xmlns='http://www.w3.org/2000/svg'><circle r='50'/></svg>".getBytes();
        MockMultipartFile file = new MockMultipartFile("file", "malicious.svg", "image/svg+xml", svgBytes);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    @DisplayName("Rejects executable files disguised as images")
    void rejectsExecutableFiles() {
        byte[] exeBytes = new byte[]{'M', 'Z', (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00, 0x04, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "avatar.exe", "image/jpeg", exeBytes);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    @DisplayName("Rejects files exceeding 5MB limit")
    void rejectsOversizedFiles() {
        byte[] largeBytes = new byte[(int) (AvatarValidationService.MAX_FILE_SIZE_BYTES + 1024)];
        // Mock JPEG magic bytes
        largeBytes[0] = (byte) 0xFF;
        largeBytes[1] = (byte) 0xD8;
        largeBytes[2] = (byte) 0xFF;

        MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeBytes);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exceeds maximum allowed size");
    }

    @Test
    @DisplayName("Rejects empty or null avatar files")
    void rejectsEmptyFile() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> validationService.validateAvatarFile(emptyFile))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot be empty");

        assertThatThrownBy(() -> validationService.validateAvatarFile(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("cannot be empty");
    }
}
