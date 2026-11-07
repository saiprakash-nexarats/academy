package com.nexarats.academy.learning;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.learning.dto.PlaybackSessionResponse;
import com.nexarats.academy.learning.model.LessonMediaAsset;
import com.nexarats.academy.learning.model.PlaybackSession;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.repository.LessonMediaAssetRepository;
import com.nexarats.academy.learning.repository.PlaybackSessionRepository;
import com.nexarats.academy.learning.service.LearnerEntitlementService;
import com.nexarats.academy.learning.service.MediaStorageService;
import com.nexarats.academy.learning.service.VideoPlaybackService;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoPlaybackServiceTest {

    @Mock
    private LearnerEntitlementService entitlementService;

    @Mock
    private LessonMediaAssetRepository mediaAssetRepository;

    @Mock
    private PlaybackSessionRepository playbackSessionRepository;

    @Mock
    private LearnerLessonProgressRepository lessonProgressRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private MediaStorageService mediaStorageService;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    private VideoPlaybackService videoPlaybackService;

    private UUID learnerUserId;
    private UUID tenantId;
    private UUID courseId;
    private UUID lessonId1;
    private UUID lessonId2;
    private Enrollment activeEnrollment;
    private Lesson lesson1;
    private Lesson lesson2;
    private LessonMediaAsset mediaAsset1;
    private LessonMediaAsset mediaAsset2;

    @BeforeEach
    void setUp() {
        videoPlaybackService = new VideoPlaybackService(
                entitlementService,
                mediaAssetRepository,
                playbackSessionRepository,
                lessonProgressRepository,
                enrollmentRepository,
                mediaStorageService,
                studentProfileRepository
        );

        learnerUserId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lessonId1 = UUID.randomUUID();
        lessonId2 = UUID.randomUUID();

        StudentProfile profile = new StudentProfile(tenantId, learnerUserId);
        profile.setOnboardingStatus(OnboardingStatus.COMPLETED);
        lenient().when(studentProfileRepository.findByTenantIdAndUserId(eq(tenantId), eq(learnerUserId)))
                .thenReturn(Optional.of(profile));

        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Distributed Systems Architecture");

        CurriculumVersion version = new CurriculumVersion();
        version.setId(UUID.randomUUID());
        version.setCourse(course);

        CurriculumModule module = new CurriculumModule();
        module.setId(UUID.randomUUID());
        module.setVersion(version);

        lesson1 = new Lesson();
        lesson1.setId(lessonId1);
        lesson1.setLessonTitle("Event Sourcing Fundamentals");
        lesson1.setDurationMinutes(30);
        lesson1.setModule(module);

        lesson2 = new Lesson();
        lesson2.setId(lessonId2);
        lesson2.setLessonTitle("CQRS and Event Store Internals");
        lesson2.setDurationMinutes(45);
        lesson2.setModule(module);

        activeEnrollment = new Enrollment("ENR-TEST-100", learnerUserId, UUID.randomUUID(), EnrollmentStatus.ENROLLED);
        activeEnrollment.setTenantId(tenantId);
        activeEnrollment.setCourseId(courseId);

        mediaAsset1 = new LessonMediaAsset(lessonId1, "VIDEO_STREAM", "media/courses/dist-sys/lesson1.mp4", 1800);
        mediaAsset1.setId(UUID.randomUUID());
        mediaAsset1.setStatus("READY");
        mediaAsset1.setStorageProvider("INTERNAL_STREAMING");

        mediaAsset2 = new LessonMediaAsset(lessonId2, "VIDEO_STREAM", "media/courses/dist-sys/lesson2.mp4", 2700);
        mediaAsset2.setId(UUID.randomUUID());
        mediaAsset2.setStatus("READY");
        mediaAsset2.setStorageProvider("INTERNAL_STREAMING");
    }

    @Test
    @DisplayName("Authorized playback session creation links lesson to its specific media asset")
    void testCreatePlaybackSessionResolvesDistinctAsset() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId1, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson1, lesson1.getModule().getVersion().getCourse(), activeEnrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId1)).thenReturn(Optional.of(mediaAsset1));
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId1, activeEnrollment.getId()))
                .thenReturn(Optional.empty());

        PlaybackSessionResponse response = videoPlaybackService.createPlaybackSession(learnerUserId, lessonId1, tenantId);

        assertThat(response.getSessionToken()).startsWith("vtok_");
        assertThat(response.getLessonId()).isEqualTo(lessonId1);
        assertThat(response.getDurationSeconds()).isEqualTo(1800);
        assertThat(response.getLastPlaybackPositionSeconds()).isEqualTo(0);
        assertThat(response.getStreamUrl()).contains("?token=" + response.getSessionToken());

        ArgumentCaptor<PlaybackSession> sessionCaptor = ArgumentCaptor.forClass(PlaybackSession.class);
        verify(playbackSessionRepository).save(sessionCaptor.capture());
        PlaybackSession saved = sessionCaptor.getValue();
        assertThat(saved.getAssetId()).isEqualTo(mediaAsset1.getId());
        assertThat(saved.getLessonId()).isEqualTo(lessonId1);
        assertThat(saved.getLearnerUserId()).isEqualTo(learnerUserId);
    }

    @Test
    @DisplayName("Playback session creation fails with 409 Conflict if media asset is not ready")
    void testCreatePlaybackSessionFailsWhenAssetNotReady() {
        mediaAsset1.setStatus("TRANSCODING");
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId1, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson1, lesson1.getModule().getVersion().getCourse(), activeEnrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId1)).thenReturn(Optional.of(mediaAsset1));

        assertThatThrownBy(() -> videoPlaybackService.createPlaybackSession(learnerUserId, lessonId1, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Media asset is not ready for playback");
    }

    @Test
    @DisplayName("Stream media resolves lesson's own storage object key and serves bytes")
    void testStreamMediaResolvesSpecificAssetStorageKey() throws IOException {
        String token = "vtok_valid_test_token_123";
        PlaybackSession session = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId1, mediaAsset1.getId(), Instant.now().plus(1, ChronoUnit.HOURS)
        );

        byte[] fakeVideoBytes = new byte[1024];
        for (int i = 0; i < fakeVideoBytes.length; i++) fakeVideoBytes[i] = (byte) (i % 128);
        Resource mockResource = new ByteArrayResource(fakeVideoBytes);

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findById(activeEnrollment.getId())).thenReturn(Optional.of(activeEnrollment));
        when(mediaAssetRepository.findById(mediaAsset1.getId())).thenReturn(Optional.of(mediaAsset1));
        when(mediaStorageService.resolveMediaResource("INTERNAL_STREAMING", "media/courses/dist-sys/lesson1.mp4"))
                .thenReturn(mockResource);

        MockHttpServletResponse response = new MockHttpServletResponse();
        videoPlaybackService.streamMedia(token, null, response);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getHeader("Content-Type")).isEqualTo("video/mp4");
        assertThat(response.getHeader("Accept-Ranges")).isEqualTo("bytes");
        assertThat(response.getContentLength()).isEqualTo(1024);
        assertThat(response.getContentAsByteArray()).hasSize(1024);
    }

    @Test
    @DisplayName("Stream media handles RFC 7233 partial content range requests")
    void testStreamMediaRfc7233ByteRange() throws IOException {
        String token = "vtok_range_test_token_456";
        PlaybackSession session = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId2, mediaAsset2.getId(), Instant.now().plus(1, ChronoUnit.HOURS)
        );

        byte[] videoBytes = new byte[2000];
        Resource mockResource = new ByteArrayResource(videoBytes);

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findById(activeEnrollment.getId())).thenReturn(Optional.of(activeEnrollment));
        when(mediaAssetRepository.findById(mediaAsset2.getId())).thenReturn(Optional.of(mediaAsset2));
        when(mediaStorageService.resolveMediaResource("INTERNAL_STREAMING", "media/courses/dist-sys/lesson2.mp4"))
                .thenReturn(mockResource);

        MockHttpServletResponse response = new MockHttpServletResponse();
        videoPlaybackService.streamMedia(token, "bytes=100-599", response);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.PARTIAL_CONTENT.value());
        assertThat(response.getHeader("Content-Range")).isEqualTo("bytes 100-599/2000");
        assertThat(response.getHeader("Content-Length")).isEqualTo("500");
        assertThat(response.getContentAsByteArray()).hasSize(500);
    }

    @Test
    @DisplayName("Stream media returns 404 when physical asset does not exist on storage provider")
    void testStreamMediaNotFoundOnStorage() {
        String token = "vtok_missing_storage_token";
        PlaybackSession session = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId1, mediaAsset1.getId(), Instant.now().plus(1, ChronoUnit.HOURS)
        );

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findById(activeEnrollment.getId())).thenReturn(Optional.of(activeEnrollment));
        when(mediaAssetRepository.findById(mediaAsset1.getId())).thenReturn(Optional.of(mediaAsset1));
        when(mediaStorageService.resolveMediaResource("INTERNAL_STREAMING", "media/courses/dist-sys/lesson1.mp4"))
                .thenReturn(null); // File does not exist on disk/storage

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> videoPlaybackService.streamMedia(token, null, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Media stream asset not found on storage provider");
    }

    @Test
    @DisplayName("Stream media is denied with 403 Forbidden if session token has expired")
    void testStreamMediaExpiredTokenForbidden() {
        String token = "vtok_expired_token";
        PlaybackSession expiredSession = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId1, mediaAsset1.getId(), Instant.now().minus(10, ChronoUnit.MINUTES)
        );

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(expiredSession));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> videoPlaybackService.streamMedia(token, null, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Playback session token has expired");
    }

    @Test
    @DisplayName("Stream media is denied with 403 Forbidden if student enrollment is no longer active")
    void testStreamMediaSuspendedEnrollmentForbidden() {
        String token = "vtok_suspended_enrollment_token";
        PlaybackSession session = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId1, mediaAsset1.getId(), Instant.now().plus(1, ChronoUnit.HOURS)
        );
        activeEnrollment.setStatus(EnrollmentStatus.SUSPENDED);

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findById(activeEnrollment.getId())).thenReturn(Optional.of(activeEnrollment));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> videoPlaybackService.streamMedia(token, null, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Enrollment is no longer active for playback");
    }

    @Test
    @DisplayName("Create playback session is denied with 403 Forbidden if student profile record is missing")
    void testCreatePlaybackSessionMissingProfileForbidden() {
        when(studentProfileRepository.findByTenantIdAndUserId(eq(tenantId), eq(learnerUserId)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> videoPlaybackService.createPlaybackSession(learnerUserId, lessonId1, tenantId, java.util.List.of("ROLE_STUDENT")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student academic onboarding must be completed before accessing course playback sessions");
    }

    @Test
    @DisplayName("Create playback session is denied with 403 Forbidden if student onboarding is IN_PROGRESS")
    void testCreatePlaybackSessionIncompleteProfileForbidden() {
        StudentProfile incomplete = new StudentProfile(tenantId, learnerUserId);
        incomplete.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        when(studentProfileRepository.findByTenantIdAndUserId(eq(tenantId), eq(learnerUserId)))
                .thenReturn(Optional.of(incomplete));

        assertThatThrownBy(() -> videoPlaybackService.createPlaybackSession(learnerUserId, lessonId1, tenantId, java.util.List.of("ROLE_STUDENT")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student academic onboarding must be completed before accessing course playback sessions");
    }

    @Test
    @DisplayName("Create playback session allows ACADEMY_ADMIN or INSTRUCTOR even if student profile is absent")
    void testCreatePlaybackSessionAdminExemptFromOnboarding() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId1, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson1, lesson1.getModule().getVersion().getCourse(), activeEnrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId1)).thenReturn(Optional.of(mediaAsset1));
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId1, activeEnrollment.getId()))
                .thenReturn(Optional.empty());

        PlaybackSessionResponse resp = videoPlaybackService.createPlaybackSession(
                learnerUserId, lessonId1, tenantId, java.util.List.of("ROLE_ACADEMY_ADMIN")
        );

        assertThat(resp).isNotNull();
        assertThat(resp.getSessionToken()).startsWith("vtok_");
        verify(studentProfileRepository, never()).findByTenantIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("Stream media is denied with 403 Forbidden if entitlement was revoked after session creation")
    void testStreamMediaRevokedEntitlementForbidden() {
        String token = "vtok_revoked_entitlement_token";
        PlaybackSession session = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId1, mediaAsset1.getId(), Instant.now().plus(1, ChronoUnit.HOURS)
        );

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findById(activeEnrollment.getId())).thenReturn(Optional.of(activeEnrollment));
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId1, tenantId))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Active enrollment required to access course content."));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> videoPlaybackService.streamMedia(token, null, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Learner entitlement has been revoked or expired");
    }

    @Test
    @DisplayName("Stream media is denied with 403 Forbidden if student profile onboarding status was reset/incomplete")
    void testStreamMediaIncompleteProfileForbidden() {
        String token = "vtok_incomplete_onboarding_token";
        PlaybackSession session = new PlaybackSession(
                token, learnerUserId, activeEnrollment.getId(), lessonId1, mediaAsset1.getId(), Instant.now().plus(1, ChronoUnit.HOURS)
        );

        when(playbackSessionRepository.findBySessionToken(token)).thenReturn(Optional.of(session));
        when(enrollmentRepository.findById(activeEnrollment.getId())).thenReturn(Optional.of(activeEnrollment));
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId1, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson1, lesson1.getModule().getVersion().getCourse(), activeEnrollment));

        StudentProfile incomplete = new StudentProfile(tenantId, learnerUserId);
        incomplete.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        when(studentProfileRepository.findByTenantIdAndUserId(eq(tenantId), eq(learnerUserId)))
                .thenReturn(Optional.of(incomplete));

        MockHttpServletResponse response = new MockHttpServletResponse();
        assertThatThrownBy(() -> videoPlaybackService.streamMedia(token, null, response))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student academic onboarding must be completed before streaming course media");
    }
}

