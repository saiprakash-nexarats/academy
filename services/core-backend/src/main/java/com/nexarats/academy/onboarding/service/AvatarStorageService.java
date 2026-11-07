package com.nexarats.academy.onboarding.service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AvatarStorageService {

    private static final Logger log = LoggerFactory.getLogger(AvatarStorageService.class);
    public static final long MAX_AVATAR_SIZE_BYTES = 5 * 1024 * 1024L; // 5 MB

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

    @Value("${nexarats.storage.s3.access-key:minio_admin}")
    private String s3AccessKey;

    @Value("${nexarats.storage.s3.secret-key:minio_secret_dev}")
    private String s3SecretKey;

    @Value("${nexarats.storage.s3.path-style-access-enabled:true}")
    private boolean pathStyleAccessEnabled;

    private S3Presigner s3Presigner;
    private S3Client s3Client;

    public record PreSignedUploadSpec(
            String uploadUrl,
            String storageObjectKey,
            String httpMethod,
            Instant expiresAt,
            String signature
    ) {
    }

    @PostConstruct
    public void init() {
        if ("S3_COMPATIBLE".equalsIgnoreCase(storageProvider) || (s3Endpoint != null && !s3Endpoint.isBlank())) {
            buildS3Clients();
        }
    }

    @PreDestroy
    public void cleanup() {
        if (s3Presigner != null) {
            try {
                s3Presigner.close();
            } catch (Exception ignored) {
            }
        }
        if (s3Client != null) {
            try {
                s3Client.close();
            } catch (Exception ignored) {
            }
        }
    }

    public synchronized void configureS3(String endpoint, String bucket, String region,
                                         String accessKey, String secretKey, boolean pathStyle) {
        this.s3Endpoint = endpoint;
        this.s3Bucket = bucket;
        this.s3Region = region != null ? region : "us-east-1";
        this.s3AccessKey = accessKey;
        this.s3SecretKey = secretKey;
        this.pathStyleAccessEnabled = pathStyle;
        this.storageProvider = "S3_COMPATIBLE";
        buildS3Clients();
    }

    private synchronized void buildS3Clients() {
        try {
            AwsCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                            s3AccessKey != null ? s3AccessKey : "minio_admin",
                            s3SecretKey != null ? s3SecretKey : "minio_secret_dev"
                    )
            );
            Region region = Region.of(s3Region != null && !s3Region.isBlank() ? s3Region : "us-east-1");

            S3Configuration serviceConfig = S3Configuration.builder()
                    .pathStyleAccessEnabled(pathStyleAccessEnabled)
                    .build();

            S3Presigner.Builder presignerBuilder = S3Presigner.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(serviceConfig);

            S3ClientBuilder clientBuilder = S3Client.builder()
                    .region(region)
                    .credentialsProvider(credentialsProvider)
                    .serviceConfiguration(serviceConfig);

            if (s3Endpoint != null && !s3Endpoint.isBlank()) {
                URI endpointUri = URI.create(s3Endpoint);
                presignerBuilder.endpointOverride(endpointUri);
                clientBuilder.endpointOverride(endpointUri);
            }

            if (this.s3Presigner != null) {
                try { this.s3Presigner.close(); } catch (Exception ignored) {}
            }
            if (this.s3Client != null) {
                try { this.s3Client.close(); } catch (Exception ignored) {}
            }

            this.s3Presigner = presignerBuilder.build();
            this.s3Client = clientBuilder.build();

            log.info("Initialized AWS SDK v2 S3Presigner and S3Client with endpoint: {}, bucket: {}, pathStyle: {}",
                    s3Endpoint, s3Bucket, pathStyleAccessEnabled);
        } catch (Exception e) {
            log.error("Failed to initialize S3 clients: {}", e.getMessage(), e);
            this.s3Presigner = null;
            this.s3Client = null;
        }
    }

    public void validateExtensionAndContentType(String extension, String contentType) {
        if (extension == null || extension.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File extension is required.");
        }
        String ext = extension.toLowerCase().replace(".", "").trim();
        if (!ext.equals("jpg") && !ext.equals("jpeg") && !ext.equals("png") && !ext.equals("webp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported image extension '." + ext + "'. Allowed formats: jpg, jpeg, png, webp.");
        }
        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content-Type is required.");
        }
        String ct = contentType.toLowerCase().trim();
        if (!ct.equals("image/jpeg") && !ct.equals("image/png") && !ct.equals("image/webp")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Unsupported Content-Type '" + ct + "'. Allowed types: image/jpeg, image/png, image/webp.");
        }
    }

    /**
     * Generates a short-lived (15 min), ownership-bound pre-signed PUT upload specification
     * for a temporary quarantine object key ONLY.
     * Clients CANNOT overwrite approved avatars because pre-signed URLs point exclusively to quarantine keys.
     */
    public PreSignedUploadSpec generatePreSignedUploadSpec(UUID tenantId, UUID userId, String fileExtension, String contentType) {
        validateExtensionAndContentType(fileExtension, contentType);
        String ext = fileExtension.toLowerCase().replace(".", "").trim();
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String quarantineKey = String.format("quarantine/avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), ext);
        Duration duration = Duration.ofMinutes(15);
        Instant expiresAt = Instant.now().plus(duration);

        if ("S3_COMPATIBLE".equalsIgnoreCase(storageProvider) || (s3Endpoint != null && !s3Endpoint.isBlank())) {
            if (s3Presigner == null) {
                log.error("S3 presigner is null while in S3_COMPATIBLE mode. Refusing nonfunctional fallback.");
                throw new IllegalStateException("AWS S3Presigner is unavailable in S3_COMPATIBLE mode.");
            }
            ensureBucketExists();
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(quarantineKey)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .putObjectRequest(putRequest)
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);
            String uploadUrl = presigned.url().toString();
            String signature = extractQueryParam(uploadUrl, "X-Amz-Signature");
            if (signature == null || signature.isBlank()) {
                signature = "sig4_" + UUID.randomUUID().toString().replace("-", "");
            }

            log.info("Generated AWS SigV4 pre-signed upload URL for user {} tenant {} quarantine key {}",
                    userId, tenantId, quarantineKey);
            return new PreSignedUploadSpec(uploadUrl, quarantineKey, "PUT", expiresAt, signature);
        } else {
            // Explicit LOCAL_STORAGE mode
            String uploadUrl = String.format("/api/v1/onboarding/avatar/quarantine-upload?key=%s", quarantineKey);
            String signature = "local_" + UUID.randomUUID().toString().replace("-", "");
            log.info("Generated local storage upload spec for user {} tenant {} quarantine key {}",
                    userId, tenantId, quarantineKey);
            return new PreSignedUploadSpec(uploadUrl, quarantineKey, "PUT", expiresAt, signature);
        }
    }

    /**
     * Inspects object existence and size using S3 HEAD object before downloading bytes.
     * Rejects empty or oversized objects and purges invalid uploads.
     */
    public long checkObjectExistsAndSize(String storageObjectKey, long maxBytes) {
        validateStorageObjectKey(storageObjectKey);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                HeadObjectResponse head = s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(storageObjectKey)
                        .build());
                long size = head.contentLength() != null ? head.contentLength() : 0L;
                if (size <= 0) {
                    deleteAvatar(storageObjectKey);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded object is empty.");
                }
                if (size > maxBytes) {
                    deleteAvatar(storageObjectKey);
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("Uploaded object exceeds maximum allowable size of %d bytes (received %d bytes)", maxBytes, size));
                }
                return size;
            } catch (NoSuchKeyException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarantined avatar object not found in storage.");
            } catch (S3Exception e) {
                if (e.statusCode() == 404) {
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarantined avatar object not found in storage.");
                }
                log.error("Failed to inspect object metadata in S3: {}", storageObjectKey, e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to inspect avatar object in storage.");
            }
        }

        Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(storageObjectKey).normalize();

        if (!targetPath.startsWith(rootPath) || !Files.exists(targetPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quarantined avatar file not found on disk.");
        }

        try {
            long size = Files.size(targetPath);
            if (size <= 0) {
                deleteAvatar(storageObjectKey);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded object is empty.");
            }
            if (size > maxBytes) {
                deleteAvatar(storageObjectKey);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        String.format("Uploaded object exceeds maximum allowable size of %d bytes (received %d bytes)", maxBytes, size));
            }
            return size;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to inspect avatar file on disk.");
        }
    }

    public byte[] getAvatarBytes(String storageObjectKey) {
        return getAvatarBytes(storageObjectKey, MAX_AVATAR_SIZE_BYTES);
    }

    /**
     * Bounded download of object bytes with hard size limit enforcement.
     */
    public byte[] getAvatarBytes(String storageObjectKey, long maxBytes) {
        validateStorageObjectKey(storageObjectKey);
        checkObjectExistsAndSize(storageObjectKey, maxBytes);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try (ResponseInputStream<GetObjectResponse> in = s3Client.getObject(
                    GetObjectRequest.builder().bucket(s3Bucket).key(storageObjectKey).build())) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                long total = 0;
                int read;
                while ((read = in.read(buffer)) != -1) {
                    total += read;
                    if (total > maxBytes) {
                        deleteAvatar(storageObjectKey);
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded avatar exceeded size limit during transfer.");
                    }
                    baos.write(buffer, 0, read);
                }
                return baos.toByteArray();
            } catch (NoSuchKeyException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar object not found in storage.");
            } catch (ResponseStatusException e) {
                throw e;
            } catch (Exception e) {
                log.error("Failed to read avatar object from S3: {}", storageObjectKey, e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read avatar from storage.");
            }
        }

        Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(storageObjectKey).normalize();

        if (!targetPath.startsWith(rootPath) || !Files.exists(targetPath)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar file bytes not found on disk.");
        }

        try {
            return Files.readAllBytes(targetPath);
        } catch (IOException e) {
            log.error("Failed to read avatar file from disk: {}", targetPath, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read avatar file.");
        }
    }

    /**
     * Atomically promotes a quarantined avatar to an unguessable approved key and deletes the quarantined file.
     */
    public String promoteQuarantinedAvatar(String quarantineKey, UUID tenantId, UUID userId, String fileExtension, byte[] verifiedBytes) {
        validateStorageObjectKey(quarantineKey);
        String ext = fileExtension.toLowerCase().replace(".", "").trim();
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String approvedKey = String.format("avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), ext);
        validateStorageObjectKey(approvedKey);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                ensureBucketExists();
                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(approvedKey)
                        .contentType(detectContentType(ext))
                        .build();
                s3Client.putObject(putReq, RequestBody.fromBytes(verifiedBytes));
                log.info("Promoted verified avatar from quarantine {} to approved key {} in S3 bucket {}",
                        quarantineKey, approvedKey, s3Bucket);
            } catch (Exception e) {
                log.error("Failed to promote avatar to approved key in S3: {}", e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist approved avatar to cloud storage.");
            }
        } else {
            Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
            Path targetPath = rootPath.resolve(approvedKey).normalize();
            if (!targetPath.startsWith(rootPath)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage path detected.");
            }
            try {
                Files.createDirectories(targetPath.getParent());
                Files.write(targetPath, verifiedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                log.info("Promoted verified avatar from quarantine {} to approved file {}", quarantineKey, targetPath);
            } catch (IOException e) {
                log.error("Failed to write approved avatar file to disk: {}", targetPath, e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist approved avatar file.");
            }
        }

        // Clean up temporary quarantined object from storage
        deleteAvatar(quarantineKey);
        return approvedKey;
    }

    public String storeAvatar(UUID tenantId, UUID userId, String extension, byte[] bytes) {
        String ext = extension.toLowerCase().replace(".", "").trim();
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String objectKey = String.format("avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), ext);

        validateStorageObjectKey(objectKey);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                ensureBucketExists();
                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(objectKey)
                        .contentType(detectContentType(ext))
                        .build();
                s3Client.putObject(putReq, RequestBody.fromBytes(bytes));
                log.info("Stored avatar asset for user {} in S3 bucket {} at key {}", userId, s3Bucket, objectKey);
                return objectKey;
            } catch (Exception e) {
                log.error("Failed to store avatar in S3: {}", e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to persist avatar to cloud storage.");
            }
        }

        Path rootPath = Paths.get(avatarUploadDir).toAbsolutePath().normalize();
        Path targetPath = rootPath.resolve(objectKey).normalize();

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

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                        GetObjectRequest.builder().bucket(s3Bucket).key(storageObjectKey).build());
                return new ByteArrayResource(objectBytes.asByteArray());
            } catch (NoSuchKeyException e) {
                log.warn("Avatar file not found in S3 bucket {} at: {}", s3Bucket, storageObjectKey);
                return null;
            } catch (Exception e) {
                log.error("Failed to retrieve avatar from S3: {}", storageObjectKey, e);
                return null;
            }
        }

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
        validateStorageObjectKey(storageObjectKey);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                s3Client.deleteObject(DeleteObjectRequest.builder().bucket(s3Bucket).key(storageObjectKey).build());
                log.info("Deleted avatar from S3 bucket {} at: {}", s3Bucket, storageObjectKey);
                return;
            } catch (Exception e) {
                log.warn("Could not delete avatar from S3 {}: {}", storageObjectKey, e.getMessage());
            }
        }

        try {
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

    public void ensureBucketExists() {
        if (s3Client != null && s3Bucket != null && !s3Bucket.isBlank()) {
            try {
                s3Client.headBucket(HeadBucketRequest.builder().bucket(s3Bucket).build());
            } catch (S3Exception ex) {
                try {
                    s3Client.createBucket(CreateBucketRequest.builder().bucket(s3Bucket).build());
                    log.info("Created private S3 bucket: {}", s3Bucket);
                } catch (Exception e) {
                    log.debug("Bucket create check: {}", e.getMessage());
                }
            }
        }
    }

    private void validateStorageObjectKey(String key) {
        if (key == null || key.isBlank() || key.contains("..") || key.startsWith("/") || key.startsWith("\\") || key.matches("^[a-zA-Z]:.*")) {
            log.warn("Invalid or traversal-prone storageObjectKey: {}", key);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid storage object key format.");
        }
    }

    private String detectContentType(String extension) {
        if (extension == null) return "application/octet-stream";
        String ext = extension.toLowerCase().replace(".", "").trim();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    private String extractQueryParam(String url, String paramName) {
        if (url == null || !url.contains("?")) return null;
        String query = url.substring(url.indexOf('?') + 1);
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && parts[0].equals(paramName)) {
                return URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    public S3Presigner getS3Presigner() {
        return s3Presigner;
    }

    public S3Client getS3Client() {
        return s3Client;
    }

    public String getStorageProvider() {
        return storageProvider;
    }
}
