package com.nexarats.academy.learning.service;

import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.learning.dto.PlaybackSessionResponse;
import com.nexarats.academy.learning.model.LearnerLessonProgress;
import com.nexarats.academy.learning.model.LessonMediaAsset;
import com.nexarats.academy.learning.model.PlaybackSession;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.repository.LessonMediaAssetRepository;
import com.nexarats.academy.learning.repository.PlaybackSessionRepository;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class VideoPlaybackService {

    private static final Logger log = LoggerFactory.getLogger(VideoPlaybackService.class);

    private final LearnerEntitlementService entitlementService;
    private final LessonMediaAssetRepository mediaAssetRepository;
    private final PlaybackSessionRepository playbackSessionRepository;
    private final LearnerLessonProgressRepository lessonProgressRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final MediaStorageService mediaStorageService;
    private final StudentProfileRepository studentProfileRepository;

    @Value("${nexarats.learning.media.demo-fallback-enabled:true}")
    private boolean demoFallbackEnabled;

    public VideoPlaybackService(LearnerEntitlementService entitlementService,
                                LessonMediaAssetRepository mediaAssetRepository,
                                PlaybackSessionRepository playbackSessionRepository,
                                LearnerLessonProgressRepository lessonProgressRepository,
                                EnrollmentRepository enrollmentRepository,
                                MediaStorageService mediaStorageService) {
        this(entitlementService, mediaAssetRepository, playbackSessionRepository,
                lessonProgressRepository, enrollmentRepository, mediaStorageService, null);
    }

    public VideoPlaybackService(LearnerEntitlementService entitlementService,
                                LessonMediaAssetRepository mediaAssetRepository,
                                PlaybackSessionRepository playbackSessionRepository,
                                LearnerLessonProgressRepository lessonProgressRepository,
                                EnrollmentRepository enrollmentRepository,
                                MediaStorageService mediaStorageService,
                                @Autowired(required = false) StudentProfileRepository studentProfileRepository) {
        this.entitlementService = entitlementService;
        this.mediaAssetRepository = mediaAssetRepository;
        this.playbackSessionRepository = playbackSessionRepository;
        this.lessonProgressRepository = lessonProgressRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.mediaStorageService = mediaStorageService;
        this.studentProfileRepository = studentProfileRepository;
    }

    @Transactional
    public PlaybackSessionResponse createPlaybackSession(UUID learnerUserId, UUID lessonId) {
        return createPlaybackSession(learnerUserId, lessonId, null);
    }

    @Transactional
    public PlaybackSessionResponse createPlaybackSession(UUID learnerUserId, UUID lessonId, UUID tenantId) {
        // Enforce onboarding access control: If student has an incomplete onboarding profile, deny playback session
        if (studentProfileRepository != null && tenantId != null) {
            Optional<StudentProfile> profileOpt = studentProfileRepository.findByTenantIdAndUserId(tenantId, learnerUserId);
            if (profileOpt.isPresent() && profileOpt.get().getOnboardingStatus() != OnboardingStatus.COMPLETED) {
                log.warn("Learner {} denied playback session: Academic onboarding is incomplete (status={})",
                        learnerUserId, profileOpt.get().getOnboardingStatus());
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Student academic onboarding must be completed before accessing course playback sessions.");
            }
        }

        LearnerEntitlementService.LessonAndEntitlement le = entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId);
        Lesson lesson = le.lesson();
        Enrollment enrollment = le.enrollment();

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED && enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            log.warn("Learner {} denied playback session: Enrollment {} is in status {}", learnerUserId, enrollment.getId(), enrollment.getStatus());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Active enrollment required for playback session.");
        }

        LessonMediaAsset asset = mediaAssetRepository.findFirstByLessonId(lessonId)
                .orElseGet(() -> {
                    if (demoFallbackEnabled) {
                        LessonMediaAsset created = new LessonMediaAsset(
                                lessonId,
                                "VIDEO_STREAM",
                                "media/sample_lesson.mp4",
                                Math.max(60, lesson.getDurationMinutes() * 60)
                        );
                        created.setStatus("READY");
                        created.setStorageProvider("INTERNAL_STREAMING");
                        return mediaAssetRepository.save(created);
                    }
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No media asset configured for lesson: " + lessonId);
                });

        if (!"READY".equalsIgnoreCase(asset.getStatus())) {
            log.warn("Media asset {} for lesson {} is not ready. Status: {}", asset.getId(), lessonId, asset.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Media asset is not ready for playback. Current status: " + asset.getStatus());
        }

        Optional<LearnerLessonProgress> lpOpt = lessonProgressRepository
                .findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId());
        int resumePosition = lpOpt.map(LearnerLessonProgress::getLastPlaybackPositionSeconds).orElse(0);

        String token = "vtok_" + UUID.randomUUID().toString().replace("-", "") + System.currentTimeMillis();
        Instant expiresAt = Instant.now().plus(Duration.ofHours(2));

        PlaybackSession session = new PlaybackSession(
                token,
                learnerUserId,
                enrollment.getId(),
                lessonId,
                asset.getId(),
                expiresAt
        );
        playbackSessionRepository.save(session);

        String streamUrl = "/api/v1/learning/media/stream?token=" + token;
        log.info("Created authorized playback session for learner {} on lesson {} (asset: {})", learnerUserId, lessonId, asset.getId());

        return new PlaybackSessionResponse(
                token,
                streamUrl,
                lessonId,
                asset.getDurationSeconds(),
                resumePosition,
                expiresAt
        );
    }

    @Transactional
    public void streamMedia(String sessionToken, String rangeHeader, HttpServletResponse response) {
        if (sessionToken == null || sessionToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing playback session token.");
        }

        PlaybackSession session = playbackSessionRepository.findBySessionToken(sessionToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid or unrecognized playback session token."));

        if (session.isExpired()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Playback session token has expired.");
        }

        // Validate enrollment authorization
        Enrollment enrollment = enrollmentRepository.findById(session.getEnrollmentId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Associated enrollment record not found."));

        if (enrollment.getStatus() != EnrollmentStatus.ENROLLED && enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            log.warn("Media stream denied: Enrollment {} is in status {}", enrollment.getId(), enrollment.getStatus());
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Enrollment is no longer active for playback.");
        }

        // Resolve LessonMediaAsset
        LessonMediaAsset asset = null;
        if (session.getAssetId() != null) {
            asset = mediaAssetRepository.findById(session.getAssetId()).orElse(null);
        }
        if (asset == null) {
            asset = mediaAssetRepository.findFirstByLessonId(session.getLessonId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Media asset record not found for lesson: " + session.getLessonId()));
        }

        if (!"READY".equalsIgnoreCase(asset.getStatus())) {
            log.warn("Media asset {} for lesson {} is not ready. Status: {}", asset.getId(), session.getLessonId(), asset.getStatus());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Media asset is not ready for streaming. Status: " + asset.getStatus());
        }

        session.setLastHeartbeatAt(Instant.now());
        playbackSessionRepository.save(session);

        // Resolve physical / cloud media resource
        Resource mediaResource = mediaStorageService.resolveMediaResource(asset.getStorageProvider(), asset.getStorageObjectKey());
        if (mediaResource == null || !mediaResource.exists()) {
            log.error("Media asset {} exists in catalog but resource not found on storage: provider={}, key={}",
                    asset.getId(), asset.getStorageProvider(), asset.getStorageObjectKey());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Media stream asset not found on storage provider for key: " + asset.getStorageObjectKey());
        }

        try {
            long fileLength = mediaResource.contentLength();
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Content-Type", "video/mp4");
            response.setHeader("Cache-Control", "private, no-transform, max-age=3600");

            if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
                String rangeValue = rangeHeader.substring(6).trim();
                long start = 0;
                long end = fileLength - 1;

                int dashIndex = rangeValue.indexOf('-');
                if (dashIndex != -1) {
                    String startStr = rangeValue.substring(0, dashIndex).trim();
                    String endStr = rangeValue.substring(dashIndex + 1).trim();

                    if (!startStr.isEmpty()) {
                        start = Long.parseLong(startStr);
                    }
                    if (!endStr.isEmpty()) {
                        end = Long.parseLong(endStr);
                    }
                }

                if (start > end || start >= fileLength) {
                    response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                    response.setHeader("Content-Range", "bytes */" + fileLength);
                    return;
                }

                if (end >= fileLength) {
                    end = fileLength - 1;
                }

                long rangeLength = end - start + 1;
                response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
                response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
                response.setHeader("Content-Length", String.valueOf(rangeLength));

                try (InputStream in = mediaResource.getInputStream();
                     OutputStream out = response.getOutputStream()) {
                    long skipped = in.skip(start);
                    byte[] buffer = new byte[8192];
                    long bytesToRead = rangeLength;
                    while (bytesToRead > 0) {
                        int read = in.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead));
                        if (read == -1) break;
                        out.write(buffer, 0, read);
                        bytesToRead -= read;
                    }
                    out.flush();
                }
            } else {
                response.setStatus(HttpStatus.OK.value());
                response.setHeader("Content-Length", String.valueOf(fileLength));
                try (InputStream in = mediaResource.getInputStream();
                     OutputStream out = response.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                    out.flush();
                }
            }
        } catch (IOException e) {
            log.warn("Media streaming transfer interrupted: {}", e.getMessage());
        }
    }
}
