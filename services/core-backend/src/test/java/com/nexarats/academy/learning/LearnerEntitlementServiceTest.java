package com.nexarats.academy.learning;

import com.nexarats.academy.academic.model.Course;
import com.nexarats.academy.academic.model.CurriculumModule;
import com.nexarats.academy.academic.model.CurriculumVersion;
import com.nexarats.academy.academic.model.Lesson;
import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.admissions.model.Enrollment;
import com.nexarats.academy.admissions.model.EnrollmentStatus;
import com.nexarats.academy.admissions.repository.EnrollmentRepository;
import com.nexarats.academy.learning.service.LearnerEntitlementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LearnerEntitlementServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    private LearnerEntitlementService entitlementService;

    private UUID learnerUserId;
    private UUID tenantId;
    private UUID otherTenantId;
    private UUID courseId;
    private UUID programId;
    private UUID lessonId;
    private Course course;
    private Lesson lesson;

    @BeforeEach
    void setUp() {
        entitlementService = new LearnerEntitlementService(
                enrollmentRepository,
                courseRepository,
                lessonRepository
        );

        learnerUserId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        otherTenantId = UUID.randomUUID();
        courseId = UUID.randomUUID();
        programId = UUID.randomUUID();
        lessonId = UUID.randomUUID();

        course = new Course();
        course.setId(courseId);
        course.setTitle("Kubernetes Operations");

        CurriculumVersion version = new CurriculumVersion();
        version.setId(UUID.randomUUID());
        version.setCourse(course);

        CurriculumModule module = new CurriculumModule();
        module.setId(UUID.randomUUID());
        module.setVersion(version);

        lesson = new Lesson();
        lesson.setId(lessonId);
        lesson.setLessonTitle("Ingress Controllers & Cert-Manager");
        lesson.setModule(module);
    }

    @Test
    @DisplayName("Direct course enrollment grants access to entitled course")
    void testDirectCourseEnrollmentEntitlement() {
        Enrollment directEnr = new Enrollment("ENR-DIR-1", learnerUserId, UUID.randomUUID(), EnrollmentStatus.ENROLLED);
        directEnr.setCourseId(courseId);
        directEnr.setTenantId(tenantId);

        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId))
                .thenReturn(List.of(directEnr));

        Enrollment verified = entitlementService.verifyCourseEntitlement(learnerUserId, courseId, tenantId);
        assertThat(verified).isNotNull();
        assertThat(verified.getCourseId()).isEqualTo(courseId);
        assertThat(entitlementService.hasCourseEntitlement(learnerUserId, courseId, tenantId)).isTrue();
    }

    @Test
    @DisplayName("Program enrollment grants access to all constituent career track courses")
    void testProgramEnrollmentCourseInheritance() {
        Enrollment progEnr = new Enrollment("ENR-PROG-1", learnerUserId, UUID.randomUUID(), EnrollmentStatus.ENROLLED);
        progEnr.setProgramId(programId);
        progEnr.setTenantId(tenantId);

        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId))
                .thenReturn(List.of(progEnr));
        when(courseRepository.findPublishedCoursesByProgramId(programId))
                .thenReturn(List.of(course));

        Enrollment verified = entitlementService.verifyCourseEntitlement(learnerUserId, courseId, tenantId);
        assertThat(verified).isNotNull();
        assertThat(verified.getProgramId()).isEqualTo(programId);
        assertThat(entitlementService.hasCourseEntitlement(learnerUserId, courseId, tenantId)).isTrue();
    }

    @Test
    @DisplayName("Withdrawn or cancelled enrollment is denied access with 403 Forbidden")
    void testWithdrawnEnrollmentDenied() {
        Enrollment withdrawnEnr = new Enrollment("ENR-WITHDRAWN", learnerUserId, UUID.randomUUID(), EnrollmentStatus.WITHDRAWN);
        withdrawnEnr.setCourseId(courseId);
        withdrawnEnr.setTenantId(tenantId);

        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId))
                .thenReturn(List.of(withdrawnEnr));

        assertThatThrownBy(() -> entitlementService.verifyCourseEntitlement(learnerUserId, courseId, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Active enrollment required");
    }

    @Test
    @DisplayName("Cross-tenant enrollment access attempt is strictly denied with 403 Forbidden")
    void testCrossTenantAccessDenied() {
        // Enrollment belongs to otherTenantId, but caller context is tenantId
        Enrollment otherTenantEnr = new Enrollment("ENR-OTHER-TENANT", learnerUserId, UUID.randomUUID(), EnrollmentStatus.ENROLLED);
        otherTenantEnr.setCourseId(courseId);
        otherTenantEnr.setTenantId(otherTenantId);

        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId))
                .thenReturn(List.of(otherTenantEnr));

        assertThatThrownBy(() -> entitlementService.verifyCourseEntitlement(learnerUserId, courseId, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Active enrollment required");
    }

    @Test
    @DisplayName("Request without tenant context is rejected with 400 Bad Request")
    void testMissingTenantContextRejected() {
        assertThatThrownBy(() -> entitlementService.verifyCourseEntitlement(learnerUserId, courseId, null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Tenant context is required");
    }

    @Test
    @DisplayName("Unauthenticated request without learner ID is rejected with 401 Unauthorized")
    void testUnauthenticatedAccessRejected() {
        assertThatThrownBy(() -> entitlementService.verifyCourseEntitlement(null, courseId, tenantId))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User must be authenticated");
    }

    @Test
    @DisplayName("Lesson entitlement verification resolves lesson and course")
    void testVerifyLessonEntitlement() {
        Enrollment enr = new Enrollment("ENR-LESSON-TEST", learnerUserId, UUID.randomUUID(), EnrollmentStatus.ENROLLED);
        enr.setCourseId(courseId);
        enr.setTenantId(tenantId);

        when(lessonRepository.findById(lessonId)).thenReturn(Optional.of(lesson));
        when(enrollmentRepository.findByLearnerUserIdOrderByEnrolledAtDesc(learnerUserId)).thenReturn(List.of(enr));

        LearnerEntitlementService.LessonAndEntitlement result = entitlementService.verifyLessonEntitlement(learnerUserId, lessonId, tenantId);

        assertThat(result.lesson().getId()).isEqualTo(lessonId);
        assertThat(result.course().getId()).isEqualTo(courseId);
        assertThat(result.enrollment().getId()).isEqualTo(enr.getId());
    }
}

