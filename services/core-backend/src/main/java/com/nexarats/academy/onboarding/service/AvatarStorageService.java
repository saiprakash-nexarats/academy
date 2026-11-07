package com.nexarats.academy.onboarding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Service
public class AvatarStorageService {

    private static final Logger log = LoggerFactory.getLogger(AvatarStorageService.class);

    @Value("${nexarats.storage.avatar-upload-dir:target/avatars}")
    private String avatarUploadDir;

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
}
