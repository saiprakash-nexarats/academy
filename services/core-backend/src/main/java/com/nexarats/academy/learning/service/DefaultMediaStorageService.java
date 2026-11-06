package com.nexarats.academy.learning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DefaultMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(DefaultMediaStorageService.class);

    private final Environment environment;

    @Value("${nexarats.learning.media.storage-root:}")
    private String storageRoot;

    @Value("${nexarats.learning.media.demo-fallback-enabled:false}")
    private boolean demoFallbackEnabled;

    public DefaultMediaStorageService(Environment environment) {
        this.environment = environment;
    }

    @Override
    public Resource resolveMediaResource(String storageProvider, String storageObjectKey) {
        if (storageObjectKey == null || storageObjectKey.isBlank()) {
            log.warn("Cannot resolve media resource: storageObjectKey is null or blank.");
            return null;
        }

        // Security check: Reject path traversal and absolute path injection
        validateStorageObjectKey(storageObjectKey);

        String provider = storageProvider != null ? storageProvider.toUpperCase() : "INTERNAL_STREAMING";

        // 1. Check ClassPath resolution (for packaged resources and demo fixtures)
        if ("INTERNAL_STREAMING".equals(provider) || "CLASSPATH".equals(provider)) {
            Resource classPathResource = new ClassPathResource(storageObjectKey);
            if (classPathResource.exists()) {
                log.debug("Resolved media asset from classpath: {}", storageObjectKey);
                return classPathResource;
            }
        }

        // 2. Check FileSystem resolution
        if ("LOCAL_STORAGE".equals(provider) || "INTERNAL_STREAMING".equals(provider)) {
            Path targetPath;
            if (storageRoot != null && !storageRoot.isBlank()) {
                Path root = Paths.get(storageRoot).toAbsolutePath().normalize();
                targetPath = root.resolve(storageObjectKey).normalize();
                if (!targetPath.startsWith(root)) {
                    log.warn("Directory traversal attempt outside storage root: {}", storageObjectKey);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Access outside configured storage root is prohibited.");
                }
            } else {
                targetPath = Paths.get(storageObjectKey).normalize();
            }

            if (Files.exists(targetPath) && Files.isReadable(targetPath)) {
                log.debug("Resolved media asset from filesystem: {}", targetPath);
                return new FileSystemResource(targetPath.toFile());
            }
        }

        // 3. Cloud storage providers (S3, MINIO) - marked incomplete / pending Phase 14
        if ("S3".equals(provider) || "MINIO".equals(provider)) {
            log.warn("Cloud storage provider {} requested for key {}. Cloud storage adapter is incomplete (scheduled for Phase 14).", provider, storageObjectKey);
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Cloud media storage provider " + provider + " is not yet implemented (scheduled for Phase 14).");
        }

        // 4. Fallback for demo media fixtures: strictly disabled by default; restricted to non-production profiles
        boolean isNonProduction = !environment.acceptsProfiles(Profiles.of("prod", "production"));
        if (demoFallbackEnabled && isNonProduction && "media/sample_lesson.mp4".equals(storageObjectKey)) {
            Resource sample = new ClassPathResource("media/sample_lesson.mp4");
            if (sample.exists()) {
                log.info("Serving demo media asset for key: {}", storageObjectKey);
                return sample;
            }
        }

        log.warn("Failed to resolve media resource for provider [{}] and key [{}]", provider, storageObjectKey);
        return null;
    }

    private void validateStorageObjectKey(String key) {
        if (key.contains("..") || key.startsWith("/") || key.startsWith("\\") || key.matches("^[a-zA-Z]:.*")) {
            log.warn("Unauthorized path or directory traversal sequence detected in storageObjectKey: {}", key);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage object key: directory traversal and absolute paths are prohibited.");
        }
    }

    @Override
    public boolean supports(String storageProvider) {
        if (storageProvider == null) return false;
        String p = storageProvider.toUpperCase();
        return p.equals("INTERNAL_STREAMING") || p.equals("LOCAL_STORAGE") || p.equals("CLASSPATH") || p.equals("S3") || p.equals("MINIO");
    }
}
