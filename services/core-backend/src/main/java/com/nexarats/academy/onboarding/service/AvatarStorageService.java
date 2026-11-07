package com.nexarats.academy.onboarding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
public class AvatarStorageService {

    private static final Logger log = LoggerFactory.getLogger(AvatarStorageService.class);

    @Value("${nexarats.storage.avatar-upload-dir:target/avatars}")
    private String avatarUploadDir;

    @Value("${nexarats.storage.provider:LOCAL_STORAGE}")
    private String storageProvider;

    @Value("${nexarats.storage.s3.bucket:nexarats-academy-avatars}")
    private String s3Bucket;

    @Value("${nexarats.storage.s3.region:us-east-1}")
    private String s3Region;

    @Value("${nexarats.storage.s3.endpoint:}")
    private String s3Endpoint;

    @Value("${nexarats.storage.presigned-signing-secret:dev-avatar-storage-signing-secret-key-32b!}")
    private String signingSecret;

    public record PreSignedUploadSpec(
            String uploadUrl,
            String storageObjectKey,
            String httpMethod,
            Instant expiresAt,
            String signature
    ) {
    }

    /**
     * Generates a short-lived (15 min), ownership-bound pre-signed upload specification.
     * Binds the target storageObjectKey to the authenticated tenantId and userId.
     */
    public PreSignedUploadSpec generatePreSignedUploadSpec(UUID tenantId, UUID userId, String fileExtension, String contentType) {
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String objectKey = String.format("avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), fileExtension);
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(15));

        String stringToSign = String.format("PUT\n%s\n%s\n%s\n%d",
                contentType, objectKey, tenantFolder, expiresAt.getEpochSecond());
        String signature = computeHmacSha256(stringToSign, signingSecret);

        String uploadUrl;
        if (s3Endpoint != null && !s3Endpoint.isBlank()) {
            uploadUrl = String.format("%s/%s/%s?X-Amz-Expires=900&signature=%s",
                    s3Endpoint.replaceAll("/$", ""), s3Bucket, objectKey, signature);
        } else {
            uploadUrl = String.format("/api/v1/onboarding/avatar/direct-upload?key=%s&sig=%s",
                    objectKey, signature);
        }

        log.info("Generated ownership-bound pre-signed upload spec for user {} tenant {} key {}",
                userId, tenantId, objectKey);

        return new PreSignedUploadSpec(uploadUrl, objectKey, "PUT", expiresAt, signature);
    }

    public String storeAvatar(UUID tenantId, UUID userId, String extension, byte[] bytes) {
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String objectKey = String.format("avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), extension);

        Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(objectKey).normalize();

        // Security check: directory traversal defense
        if (!targetPath.startsWith(rootPath)) {
            log.warn("Directory traversal detected in avatar storage path: {}", targetPath);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage path detected.");
        }

        try {
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Stored avatar asset for user {} at {}", userId, targetPath);
            return objectKey;
        } catch (IOException e) {
            log.error("Failed to write avatar file to disk: {}", targetPath, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist avatar file.");
        }
    }

    public Resource loadAvatarResource(String storageObjectKey) {
        validateStorageObjectKey(storageObjectKey);

        Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(storageObjectKey).normalize();

        if (!targetPath.startsWith(rootPath)) {
            log.warn("Unauthorized traversal attempt for key: {}", storageObjectKey);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage key.");
        }

        if (!Files.exists(targetPath) || !Files.isReadable(targetPath)) {
            log.warn("Avatar file not found at: {}", targetPath);
            return null;
        }

        return new FileSystemResource(targetPath.toFile());
    }

    public void deleteAvatar(String storageObjectKey) {
        if (storageObjectKey == null || storageObjectKey.isBlank()) {
            return;
        }
        try {
            validateStorageObjectKey(storageObjectKey);
            Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
            Path targetPath = rootPath.resolve(storageObjectKey).normalize();
            if (targetPath.startsWith(rootPath) && Files.exists(targetPath)) {
                Files.delete(targetPath);
                log.info("Deleted avatar file at: {}", targetPath);
            }
        } catch (Exception e) {
            log.warn("Could not delete avatar file {}: {}", storageObjectKey, e.getMessage());
        }
    }

    private void validateStorageObjectKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/") || key.startsWith("\\") || key.matches("^[a-zA-Z]:.*")) {
            log.warn("Invalid or traversal-prone storageObjectKey: {}", key);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage object key format.");
        }
    }

    private String computeHmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(rawHmac);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256 signature", e);
        }
    }
}
