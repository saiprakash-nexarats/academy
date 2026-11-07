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

    private byte[] createTestWebpBytes(int width, int height) {
        byte[] bytes = new byte[30];
        // 0..3: RIFF
        bytes[0] = 'R'; bytes[1] = 'I'; bytes[2] = 'F'; bytes[3] = 'F';
        // 4..7: File size (22 bytes after RIFF header)
        bytes[4] = 22; bytes[5] = 0; bytes[6] = 0; bytes[7] = 0;
        // 8..11: WEBP
        bytes[8] = 'W'; bytes[9] = 'E'; bytes[10] = 'B'; bytes[11] = 'P';
        // 12..15: VP8
        bytes[12] = 'V'; bytes[13] = 'P'; bytes[14] = '8'; bytes[15] = ' ';
        // 16..19: Chunk size (10 bytes)
        bytes[16] = 10; bytes[17] = 0; bytes[18] = 0; bytes[19] = 0;
        // 20..22: Frame header
        bytes[20] = 0; bytes[21] = 0; bytes[22] = 0;
        // 23..25: Start code 0x9D 0x01 0x2A
        bytes[23] = (byte) 0x9D; bytes[24] = 0x01; bytes[25] = 0x2A;
        // 26..27: Width (little endian 14 bits)
        bytes[26] = (byte) (width & 0xFF);
        bytes[27] = (byte) ((width >> 8) & 0x3F);
        // 28..29: Height (little endian 14 bits)
        bytes[28] = (byte) (height & 0xFF);
        bytes[29] = (byte) ((height >> 8) & 0x3F);
        return bytes;
    }

    @Test
    @DisplayName("Successfully validates legitimate JPEG image with real dimensions")
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
    @DisplayName("Successfully validates legitimate PNG image with real dimensions")
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
    @DisplayName("Accurately parses real WebP VP8 dimensions without placeholder fabrication")
    void validatesValidWebPDimensions() {
        byte[] webpBytes = createTestWebpBytes(320, 240);
        MockMultipartFile file = new MockMultipartFile("file", "avatar.webp", "image/webp", webpBytes);

        AvatarValidationService.ValidatedAvatar result = validationService.validateAvatarFile(file);

        assertThat(result).isNotNull();
        assertThat(result.detectedContentType()).isEqualTo("image/webp");
        assertThat(result.fileExtension()).isEqualTo("webp");
        assertThat(result.width()).isEqualTo(320);
        assertThat(result.height()).isEqualTo(240);
    }

    @Test
    @DisplayName("Rejects malformed or truncated WebP image")
    void rejectsTruncatedWebP() {
        byte[] truncatedWebp = new byte[]{'R', 'I', 'F', 'F', 10, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8'};
        MockMultipartFile file = new MockMultipartFile("file", "corrupt.webp", "image/webp", truncatedWebp);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("header too short");
    }

    @Test
    @DisplayName("Rejects corrupted/truncated JPEG image")
    void rejectsTruncatedJpeg() {
        byte[] fakeJpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00, 0x10, 'J', 'F', 'I', 'F', 0, 1, 1};
        MockMultipartFile file = new MockMultipartFile("file", "bad.jpg", "image/jpeg", fakeJpeg);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Corrupted or invalid image data");
    }

    @Test
    @DisplayName("Rejects image with dimensions exceeding 4096px maximum limit")
    void rejectsOversizedDimensions() {
        byte[] hugeWebp = createTestWebpBytes(4097, 100);
        MockMultipartFile file = new MockMultipartFile("file", "huge.webp", "image/webp", hugeWebp);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("out of bounds");
    }

    @Test
    @DisplayName("Rejects image with dimensions smaller than 32px minimum limit")
    void rejectsUndersizedDimensions() {
        byte[] tinyWebp = createTestWebpBytes(16, 16);
        MockMultipartFile file = new MockMultipartFile("file", "tiny.webp", "image/webp", tinyWebp);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("out of bounds");
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
    @DisplayName("Rejects executable files disguised with image filenames")
    void rejectsExecutables() {
        byte[] exeBytes = new byte[]{'M', 'Z', (byte) 0x90, 0x00, 0x03, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("file", "trojan.jpg", "image/jpeg", exeBytes);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Invalid file type");
    }

    @Test
    @DisplayName("Rejects files exceeding 5 MB limit")
    void rejectsFilesExceeding5Mb() {
        byte[] largeBytes = new byte[5 * 1024 * 1024 + 1024];
        largeBytes[0] = (byte) 0xFF;
        largeBytes[1] = (byte) 0xD8;
        largeBytes[2] = (byte) 0xFF;

        MockMultipartFile file = new MockMultipartFile("file", "large.jpg", "image/jpeg", largeBytes);

        assertThatThrownBy(() -> validationService.validateAvatarFile(file))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exceeds maximum allowed size");
    }
}
