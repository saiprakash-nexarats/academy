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
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
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

    @Value("${nexarats.storage.s3.access-key:minio_admin}")
    private String s3AccessKey;

    @Value("${nexarats.storage.s3.secret-key:minio_secret_dev}")
    private String s3SecretKey;

    @Value("${nexarats.storage.s3.path-style-access-enabled:true}")
    private boolean pathStyleAccessEnabled;

    @Value("${nexarats.storage.presigned-signing-secret:dev-avatar-storage-signing-secret-key-32b!}")
    private String signingSecret;

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
            log.warn("Failed to initialize S3 clients: {}", e.getMessage());
        }
    }

    /**
     * Generates a short-lived (15 min), ownership-bound pre-signed PUT upload specification.
     * When S3/MinIO is configured, generates a real AWS SigV4 pre-signed PUT URL.
     * Binds the target storageObjectKey to the authenticated tenantId and userId.
     */
    public PreSignedUploadSpec generatePreSignedUploadSpec(UUID tenantId, UUID userId, String fileExtension, String contentType) {
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String objectKey = String.format("avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), fileExtension);
        Duration duration = Duration.ofMinutes(15);
        Instant expiresAt = Instant.now().plus(duration);

        if (s3Presigner != null) {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Bucket)
                    .key(objectKey)
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

            log.info("Generated AWS SigV4 pre-signed upload URL for user {} tenant {} key {}", userId, tenantId, objectKey);
            return new PreSignedUploadSpec(uploadUrl, objectKey, "PUT", expiresAt, signature);
        } else {
            String stringToSign = String.format("PUT\n%s\n%s\n%s\n%d",
                    contentType, objectKey, tenantFolder, expiresAt.getEpochSecond());
            String signature = computeHmacSha256(stringToSign, signingSecret);

            String uploadUrl;
            if (s3Endpoint != null && !s3Endpoint.isBlank()) {
                uploadUrl = String.format("%s/%s/%s?X-Amz-Expires=900&X-Amz-Signature=%s",
                        s3Endpoint.replaceAll("/$", ""), s3Bucket, objectKey, signature);
            } else {
                uploadUrl = String.format("/api/v1/onboarding/avatar/upload-spec?key=%s&sig=%s",
                        objectKey, signature);
            }

            log.info("Generated pre-signed upload spec (fallback) for user {} tenant {} key {}",
                    userId, tenantId, objectKey);
            return new PreSignedUploadSpec(uploadUrl, objectKey, "PUT", expiresAt, signature);
        }
    }

    public String storeAvatar(UUID tenantId, UUID userId, String extension, byte[] bytes) {
        String tenantFolder = tenantId != null ? tenantId.toString() : "global";
        String objectKey = String.format("avatars/%s/%s/%s.%s", tenantFolder, userId, UUID.randomUUID(), extension);

        validateStorageObjectKey(objectKey);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                ensureBucketExists();
                PutObjectRequest putReq = PutObjectRequest.builder()
                        .bucket(s3Bucket)
                        .key(objectKey)
                        .contentType(detectContentType(extension))
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

    public byte[] getAvatarBytes(String storageObjectKey) {
        validateStorageObjectKey(storageObjectKey);

        if (s3Client != null && "S3_COMPATIBLE".equalsIgnoreCase(storageProvider)) {
            try {
                return s3Client.getObjectAsBytes(
                        GetObjectRequest.builder().bucket(s3Bucket).key(storageObjectKey).build()).asByteArray();
            } catch (NoSuchKeyException e) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Avatar object not found in storage.");
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
        String ext = extension.toLowerCase().replace(".", "");
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

    public S3Presigner getS3Presigner() {
        return s3Presigner;
    }

    public S3Client getS3Client() {
        return s3Client;
    }
}
