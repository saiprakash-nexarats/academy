package com.nexarats.academy.learning.service;

import org.springframework.core.io.Resource;

/**
 * Storage abstraction for resolving course lesson media assets.
 * Decouples video delivery from specific physical or cloud storage providers (Local, Classpath, S3, MinIO).
 */
public interface MediaStorageService {

    /**
     * Resolves a media resource based on the storage provider and storage object key.
     *
     * @param storageProvider e.g. "INTERNAL_STREAMING", "LOCAL_STORAGE", "CLASSPATH", "S3", "MINIO"
     * @param storageObjectKey relative or absolute path / object key
     * @return the resolved Spring Resource, or null if unresolvable
     */
    Resource resolveMediaResource(String storageProvider, String storageObjectKey);

    /**
     * Checks if the storage provider is supported by this storage service.
     */
    boolean supports(String storageProvider);
}

