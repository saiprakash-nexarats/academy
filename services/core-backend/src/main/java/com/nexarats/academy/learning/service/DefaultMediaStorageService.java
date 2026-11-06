package com.nexarats.academy.learning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DefaultMediaStorageService implements MediaStorageService {

    private static final Logger log = LoggerFactory.getLogger(DefaultMediaStorageService.class);

    @Value("${nexarats.learning.media.storage-root:}")
    private String storageRoot;

    @Value("${nexarats.learning.media.demo-fallback-enabled:true}")
    private boolean demoFallbackEnabled;

    @Override
    public Resource resolveMediaResource(String storageProvider, String storageObjectKey) {
        if (storageObjectKey == null || storageObjectKey.isBlank()) {
            log.warn("Cannot resolve media resource: storageObjectKey is null or blank.");
            return null;
        }

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
                targetPath = Paths.get(storageRoot, storageObjectKey);
            } else {
                targetPath = Paths.get(storageObjectKey);
            }

            if (Files.exists(targetPath) && Files.isReadable(targetPath)) {
                log.debug("Resolved media asset from filesystem: {}", targetPath);
                return new FileSystemResource(targetPath.toFile());
            }
        }

        // 3. Cloud storage providers (S3, MINIO) - hook for Phase 14
        if ("S3".equals(provider) || "MINIO".equals(provider)) {
            log.info("Cloud storage provider {} requested for key {}. Cloud storage adapter hook ready for Phase 14.", provider, storageObjectKey);
            return null;
        }

        // 4. Fallback for demo media fixtures when configured
        if (demoFallbackEnabled && "media/sample_lesson.mp4".equals(storageObjectKey)) {
            Resource sample = new ClassPathResource("media/sample_lesson.mp4");
            if (sample.exists()) {
                log.info("Serving demo media asset for key: {}", storageObjectKey);
                return sample;
            }
        }

        log.warn("Failed to resolve media resource for provider [{}] and key [{}]", provider, storageObjectKey);
        return null;
    }

    @Override
    public boolean supports(String storageProvider) {
        if (storageProvider == null) return false;
        String p = storageProvider.toUpperCase();
        return p.equals("INTERNAL_STREAMING") || p.equals("LOCAL_STORAGE") || p.equals("CLASSPATH") || p.equals("S3") || p.equals("MINIO");
    }
}

