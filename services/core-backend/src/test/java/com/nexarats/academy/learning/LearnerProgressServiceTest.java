package com.nexarats.academy.learning;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.learning.dto.PlaybackCheckpointRequest;
import com.nexarats.academy.learning.dto.PlaybackCheckpointResponse;
import com.nexarats.academy.learning.model.LearnerCourseProgress;
import com.nexarats.academy.learning.model.LearnerLessonProgress;
import com.nexarats.academy.learning.model.LearningActivityRecord;
import com.nexarats.academy.learning.model.LessonMediaAsset;
import com.nexarats.academy.learning.model.PlaybackSession;
import com.nexarats.academy.learning.repository.LearnerCourseProgressRepository;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.repository.LearningActivityRecordRepository;
import com.nexarats.academy.learning.repository.LessonMediaAssetRepository;
import com.nexarats.academy.learning.repository.PlaybackSessionRepository;
import com.nexarats.academy.learning.service.LearnerEntitlementService;
import com.nexarats.academy.learning.service.LearnerProgressService;
import com.nexarats.academy.onboarding.model.OnboardingStatus;
import com.nexarats.academy.onboarding.model.StudentProfile;
import com.nexarats.academy.onboarding.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LearnerProgressServiceTest {

    @Mock
    private LearnerEntitlementService entitlementService;

    @Mock
    private LearnerCourseProgressRepository courseProgressRepository;

    @Mock
    private LearnerLessonProgressRepository lessonProgressRepository;

    @Mock
    private LearningActivityRecordRepository activityRecordRepository;

    @Mock
    private LessonMediaAssetRepository mediaAssetRepository;

    @Mock
    private PlaybackSessionRepository playbackSessionRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudentProfileRepository studentProfileRepository;

    private LearnerProgressService progressService;

    private UUID learnerUserId;
    private UUID tenantId;
    private UUID courseId;
    private UUID lessonId;
    private Enrollment enrollment;
    private Course course;
    private CurriculumVersion version;
    private Lesson lesson;
    private LessonMediaAsset mediaAsset;

    @BeforeEach
    void setUp() {
        progressService = new LearnerProgressService(
                entitlementService,
                courseProgressRepository,
                lessonProgressRepository,
                activityRecordRepository,
                mediaAssetRepository,
                playbackSessionRepository,
                courseRepository,
                studentProfileRepository
        );

        learnerUserId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        lessonId = UUID.randomUUID();

        StudentProfile profile = new StudentProfile(tenantId, learnerUserId);
        profile.setOnboardingStatus(OnboardingStatus.COMPLETED);
        org.mockito.Mockito.lenient().when(studentProfileRepository.findByTenantIdAndUserId(org.mockito.ArgumentMatchers.eq(tenantId), org.mockito.ArgumentMatchers.eq(learnerUserId)))
                .thenReturn(Optional.of(profile));

        course = new Course();
        course.setId(courseId);
        course.setTitle("Cloud Native Microservices");

        version = new CurriculumVersion();
        version.setId(UUID.randomUUID());
        version.setCourse(course);
        version.setStatus("PUBLISHED");

        CurriculumModule module = new CurriculumModule();
        module.setId(UUID.randomUUID());
        module.setVersion(version);

        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setLessonTitle("Service Mesh & Istio");
        lesson.setLessonType("LECTURE");
        lesson.setDurationMinutes(20); // 1200 seconds
        lesson.setModule(module);

        module.getLessons().add(lesson);
        version.getModules().add(module);
        course.setPublishedVersion(version);

        enrollment = new Enrollment("ENR-TEST-200", learnerUserId, UUID.randomUUID(), EnrollmentStatus.ENROLLED);
        enrollment.setId(UUID.randomUUID());
        enrollment.setTenantId(tenantId);
        enrollment.setCourseId(courseId);

        mediaAsset = new LessonMediaAsset(lessonId, "VIDEO_STREAM", "media/courses/cloud/lesson1.mp4", 1200);
    }

    @Test
    @DisplayName("Valid progress checkpoint increments watch time and updates playback position")
    void testRecordValidProgressCheckpoint() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(activityRecordRepository.findByIdempotencyKey("chk_key_101")).thenReturn(Optional.empty());

        LearnerLessonProgress existingLp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        existingLp.setWatchTimeSeconds(50);
        existingLp.setLastPlaybackPositionSeconds(50);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(existingLp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(70, 20, "chk_key_101");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.getLastPlaybackPositionSeconds()).isEqualTo(70);
        assertThat(response.getWatchTimeSeconds()).isEqualTo(70); // 50 + 20
        assertThat(response.isLessonCompleted()).isFalse();
        verify(activityRecordRepository).save(any(LearningActivityRecord.class));
    }

    @Test
    @DisplayName("Forward seek beyond 90% does NOT mark lesson completed without accumulated watch time")
    void testForwardSeekBeyond90PercentDoesNotCompleteLesson() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(activityRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(15);
        lp.setLastPlaybackPositionSeconds(15);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // User scrubs/seeks playhead to 1150s (95.8% of 1200s), but watch delta is only 5s
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(1150, 5, "seek_key_95");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.getLastPlaybackPositionSeconds()).isEqualTo(1150);
        assertThat(response.getWatchTimeSeconds()).isEqualTo(20); // 15 + 5
        assertThat(response.isLessonCompleted()).isFalse();
        assertThat(response.getLessonCompletionPercentage()).isLessThan(100);
        verify(lessonProgressRepository).save(lp);
    }

    @Test
    @DisplayName("Meaningful accumulated watch time >= 80% marks lesson completed")
    void testAccumulatedWatchTimeCompletesLesson() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(activityRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(950); // 80% of 1200 is 960s
        lp.setLastPlaybackPositionSeconds(950);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));
        when(lessonProgressRepository.countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(1L);

        // Adds 20s watch time -> total 970s >= 960s threshold
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(970, 20, "watch_complete_key");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.isLessonCompleted()).isTrue();
        assertThat(response.getLessonCompletionPercentage()).isEqualTo(100);
        assertThat(response.getCourseCompletionPercentage()).isEqualTo(100);
    }

    @Test
    @DisplayName("Pause checkpoint preserves resume position without incrementing watch time")
    void testPauseCheckpointPreservesState() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(activityRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(300);
        lp.setLastPlaybackPositionSeconds(300);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // Paused event: delta is 0
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(300, 0, "pause_key");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.getLastPlaybackPositionSeconds()).isEqualTo(300);
        assertThat(response.getWatchTimeSeconds()).isEqualTo(300);
        assertThat(response.isLessonCompleted()).isFalse();
    }

    @Test
    @DisplayName("Replay checkpoint updates playback position backward without losing accumulated watch time")
    void testReplayPreservesWatchTime() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(activityRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(400);
        lp.setLastPlaybackPositionSeconds(400);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // Replaying earlier section: position jumps backward to 50s with 10s of watched time
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(50, 10, "replay_key");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.getLastPlaybackPositionSeconds()).isEqualTo(50);
        assertThat(response.getWatchTimeSeconds()).isEqualTo(410); // 400 + 10
        assertThat(response.isLessonCompleted()).isFalse();
    }

    @Test
    @DisplayName("Replay checkpoint submission with identical idempotency key is safely deduplicated")
    void testIdempotentCheckpointReplay() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));

        LearningActivityRecord pastRecord = new LearningActivityRecord(learnerUserId, enrollment.getId(), courseId, lessonId, "PLAYBACK_CHECKPOINT");
        pastRecord.setIdempotencyKey("duplicate_key_777");
        when(activityRecordRepository.findByIdempotencyKey("duplicate_key_777")).thenReturn(Optional.of(pastRecord));

        LearnerLessonProgress currentLp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        currentLp.setWatchTimeSeconds(100);
        currentLp.setLastPlaybackPositionSeconds(100);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(currentLp));

        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(100, 30, "duplicate_key_777");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.getMessage()).isEqualTo("Idempotent duplicate ignored.");
        assertThat(response.getWatchTimeSeconds()).isEqualTo(100); // Unchanged!
        verify(lessonProgressRepository, never()).save(any());
    }

    @Test
    @DisplayName("Checkpoint bound to playback session validates session ownership and server elapsed time")
    void testCheckpointBoundToSessionValidatesSessionAndHeartbeat() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(activityRecordRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        // Session was active 10 seconds ago
        PlaybackSession session = new PlaybackSession("valid_token_xyz", learnerUserId, enrollment.getId(), lessonId, mediaAsset.getId(), Instant.now().plus(1, ChronoUnit.HOURS));
        session.setLastHeartbeatAt(Instant.now().minus(10, ChronoUnit.SECONDS));
        when(playbackSessionRepository.findBySessionToken("valid_token_xyz")).thenReturn(Optional.of(session));

        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // Submitting reasonable delta: 10s watch delta after 10s elapsed
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(10, 10, "session_chk_1", "valid_token_xyz");
        PlaybackCheckpointResponse response = progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId);

        assertThat(response.getLastPlaybackPositionSeconds()).isEqualTo(10);
        assertThat(response.getWatchTimeSeconds()).isEqualTo(10);
        verify(playbackSessionRepository).save(session);
    }

    @Test
    @DisplayName("Fabricated checkpoint delta far exceeding server elapsed time is rejected with 400 Bad Request")
    void testFabricatedCheckpointDeltaRejected() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));

        // Session heartbeat was only 2 seconds ago
        PlaybackSession session = new PlaybackSession("valid_token_xyz", learnerUserId, enrollment.getId(), lessonId, mediaAsset.getId(), Instant.now().plus(1, ChronoUnit.HOURS));
        session.setLastHeartbeatAt(Instant.now().minus(2, ChronoUnit.SECONDS));
        when(playbackSessionRepository.findBySessionToken("valid_token_xyz")).thenReturn(Optional.of(session));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // Client falsely claims 60s watch delta in 2 seconds of server time
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(60, 60, "cheat_key_1", "valid_token_xyz");

        assertThatThrownBy(() -> progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("exceeds server-verified elapsed session time");
    }

    @Test
    @DisplayName("Checkpoint with session belonging to different user is rejected with 403 Forbidden")
    void testCheckpointSessionBelongingToDifferentUserRejected() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));

        UUID otherUser = UUID.randomUUID();
        PlaybackSession session = new PlaybackSession("stolen_token", otherUser, enrollment.getId(), lessonId, mediaAsset.getId(), Instant.now().plus(1, ChronoUnit.HOURS));
        when(playbackSessionRepository.findBySessionToken("stolen_token")).thenReturn(Optional.of(session));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(10, 5, "stolen_chk", "stolen_token");

        assertThatThrownBy(() -> progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Playback session does not belong to the authenticated learner");
    }

    @Test
    @DisplayName("Explicit completion of video lesson WITHOUT 80% watch time is rejected with 400 Bad Request")
    void testExplicitVideoCompletionWithoutSufficientWatchTimeRejected() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // User only watched 200s out of 1200s (required 960s)
        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(200);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));

        // Regular student role attempts to mark video lesson complete
        assertThatThrownBy(() -> progressService.markLessonComplete(learnerUserId, lessonId, tenantId, List.of("ROLE_STUDENT")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Video lesson completion requires at least 80% validated watch time");
    }

    @Test
    @DisplayName("Explicit completion of video lesson WITH >= 80% watch time succeeds")
    void testExplicitVideoCompletionWithSufficientWatchTimeSucceeds() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // User watched 1000s out of 1200s (threshold 960s)
        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(1000);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(lessonProgressRepository.countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(1L);

        PlaybackCheckpointResponse response = progressService.markLessonComplete(learnerUserId, lessonId, tenantId, List.of("ROLE_STUDENT"));

        assertThat(response.isLessonCompleted()).isTrue();
        assertThat(response.getLessonCompletionPercentage()).isEqualTo(100);
        verify(activityRecordRepository).save(any(LearningActivityRecord.class));
    }

    @Test
    @DisplayName("Explicit completion of non-video reading lesson succeeds immediately for student")
    void testExplicitNonVideoLessonCompletionSucceeds() {
        Lesson readingLesson = new Lesson();
        readingLesson.setId(lessonId);
        readingLesson.setLessonTitle("Architecture Blueprint Reading");
        readingLesson.setLessonType("EXERCISE");
        readingLesson.setDurationMinutes(15);
        readingLesson.setModule(lesson.getModule());

        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(readingLesson, course, enrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.empty()); // No video asset!

        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(lessonProgressRepository.countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(1L);

        PlaybackCheckpointResponse response = progressService.markLessonComplete(learnerUserId, lessonId, tenantId, List.of("ROLE_STUDENT"));

        assertThat(response.isLessonCompleted()).isTrue();
        assertThat(response.getLessonCompletionPercentage()).isEqualTo(100);
    }

    @Test
    @DisplayName("Admin workflow override permits completing video lesson even under 80% watch time")
    void testAdminBypassPermitsVideoCompletion() {
        when(entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId))
                .thenReturn(new LearnerEntitlementService.LessonAndEntitlement(lesson, course, enrollment));
        when(mediaAssetRepository.findFirstByLessonId(lessonId)).thenReturn(Optional.of(mediaAsset));

        // User watched 0s
        LearnerLessonProgress lp = new LearnerLessonProgress(learnerUserId, enrollment.getId(), courseId, lessonId);
        lp.setWatchTimeSeconds(0);
        when(lessonProgressRepository.findByLearnerUserIdAndLessonIdAndEnrollmentId(learnerUserId, lessonId, enrollment.getId()))
                .thenReturn(Optional.of(lp));
        when(lessonProgressRepository.save(any(LearnerLessonProgress.class))).thenAnswer(inv -> inv.getArgument(0));

        LearnerCourseProgress courseProg = new LearnerCourseProgress(learnerUserId, enrollment.getId(), courseId, tenantId);
        when(courseProgressRepository.findByLearnerUserIdAndCourseIdAndEnrollmentId(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(Optional.of(courseProg));
        when(lessonProgressRepository.countByLearnerUserIdAndCourseIdAndEnrollmentIdAndIsCompletedTrue(learnerUserId, courseId, enrollment.getId()))
                .thenReturn(1L);

        // Admin workflow
        PlaybackCheckpointResponse response = progressService.markLessonComplete(learnerUserId, lessonId, tenantId, List.of("ROLE_ACADEMY_ADMIN"));

        assertThat(response.isLessonCompleted()).isTrue();
        assertThat(response.getLessonCompletionPercentage()).isEqualTo(100);
    }

    @Test
    @DisplayName("Checkpoint with negative playback position is rejected with 400 Bad Request")
    void testNegativePlaybackPositionRejected() {
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(-10, 15, "neg_key");
        assertThatThrownBy(() -> progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Playback position cannot be negative");
    }

    @Test
    @DisplayName("Checkpoint with watch time delta > 300s is rejected with 400 Bad Request")
    void testExcessiveWatchTimeDeltaRejected() {
        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(500, 301, "spike_key");
        assertThatThrownBy(() -> progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unrealistic watch time delta");
    }

    @Test
    @DisplayName("Progress checkpoint is denied with 403 Forbidden if student profile record is missing")
    void testRecordCheckpointMissingStudentProfileForbidden() {
        org.mockito.Mockito.when(studentProfileRepository.findByTenantIdAndUserId(tenantId, learnerUserId))
                .thenReturn(Optional.empty());

        PlaybackCheckpointRequest request = new PlaybackCheckpointRequest(60, 15, "chk_miss_profile");
        assertThatThrownBy(() -> progressService.recordCheckpoint(learnerUserId, lessonId, request, tenantId, List.of("ROLE_STUDENT")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student academic onboarding must be completed before recording lesson progress");
    }

    @Test
    @DisplayName("Mark lesson complete is denied with 403 Forbidden if student onboarding is incomplete")
    void testMarkLessonCompleteIncompleteStudentProfileForbidden() {
        StudentProfile incomplete = new StudentProfile(tenantId, learnerUserId);
        incomplete.setOnboardingStatus(OnboardingStatus.IN_PROGRESS);
        org.mockito.Mockito.when(studentProfileRepository.findByTenantIdAndUserId(tenantId, learnerUserId))
                .thenReturn(Optional.of(incomplete));

        assertThatThrownBy(() -> progressService.markLessonComplete(learnerUserId, lessonId, tenantId, List.of("ROLE_STUDENT")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Student academic onboarding must be completed before marking lessons complete");
    }
}
