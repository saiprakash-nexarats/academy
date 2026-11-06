package com.nexarats.academy.learning;

import com.nexarats.academy.academic.repository.CourseRepository;
import com.nexarats.academy.academic.repository.CurriculumModuleRepository;
import com.nexarats.academy.academic.repository.CurriculumVersionRepository;
import com.nexarats.academy.academic.repository.LessonRepository;
import com.nexarats.academy.common.security.UserPrincipal;
import com.nexarats.academy.learning.dto.LearnerDashboardResponse;
import com.nexarats.academy.learning.repository.LearnerCourseProgressRepository;
import com.nexarats.academy.learning.repository.LearnerLessonProgressRepository;
import com.nexarats.academy.learning.service.LearnerDashboardService;
import com.nexarats.academy.learning.service.LearnerEntitlementService;
import com.nexarats.academy.learning.service.LearnerProgressService;
import com.nexarats.academy.learning.service.VideoPlaybackService;
import com.nexarats.academy.learning.web.LearningController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LearningControllerSecurityTest {

    @Mock
    private LearnerDashboardService dashboardService;

    @Mock
    private LearnerEntitlementService entitlementService;

    @Mock
    private LearnerProgressService progressService;

    @Mock
    private VideoPlaybackService playbackService;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private LessonRepository lessonRepository;

    @Mock
    private CurriculumVersionRepository versionRepository;

    @Mock
    private CurriculumModuleRepository moduleRepository;

    @Mock
    private LearnerCourseProgressRepository courseProgressRepository;

    @Mock
    private LearnerLessonProgressRepository lessonProgressRepository;

    private LearningController learningController;

    private UUID userId;
    private UUID tenantId;

    @BeforeEach
    void setUp() {
        learningController = new LearningController(
                dashboardService,
                entitlementService,
                progressService,
                playbackService,
                courseRepository,
                lessonRepository,
                versionRepository,
                moduleRepository,
                courseProgressRepository,
                lessonProgressRepository
        );

        userId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
    }

    private UserPrincipal createPrincipal(String role) {
        return new UserPrincipal(
                userId,
                "testuser@example.com",
                "Test User",
                tenantId,
                "default",
                UUID.randomUUID(),
                Set.of(role),
                Set.of()
        );
    }

    @Test
    @DisplayName("ROLE_STUDENT is authorized to access learner dashboard")
    void testStudentAuthorizedDashboard() {
        UserPrincipal principal = createPrincipal("ROLE_STUDENT");
        LearnerDashboardResponse mockResp = new LearnerDashboardResponse(
                userId, "Test User", "testuser@example.com", 1, 0, 10, null, List.of(), List.of()
        );
        when(dashboardService.getDashboard(eq(userId), eq(tenantId))).thenReturn(mockResp);

        ResponseEntity<LearnerDashboardResponse> response = learningController.getDashboard(principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getFullName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("ROLE_EMPLOYEE is authorized to access learner dashboard")
    void testEmployeeAuthorizedDashboard() {
        UserPrincipal principal = createPrincipal("ROLE_EMPLOYEE");
        LearnerDashboardResponse mockResp = new LearnerDashboardResponse(
                userId, "Corp Learner", "corp@example.com", 2, 1, 60, null, List.of(), List.of()
        );
        when(dashboardService.getDashboard(eq(userId), eq(tenantId))).thenReturn(mockResp);

        ResponseEntity<LearnerDashboardResponse> response = learningController.getDashboard(principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getFullName()).isEqualTo("Corp Learner");
    }

    @Test
    @DisplayName("ROLE_ACADEMY_ADMIN is authorized to access learner dashboard")
    void testAdminAuthorizedDashboard() {
        UserPrincipal principal = createPrincipal("ROLE_ACADEMY_ADMIN");
        LearnerDashboardResponse mockResp = new LearnerDashboardResponse(
                userId, "Admin User", "admin@example.com", 0, 0, 0, null, List.of(), List.of()
        );
        when(dashboardService.getDashboard(eq(userId), eq(tenantId))).thenReturn(mockResp);

        ResponseEntity<LearnerDashboardResponse> response = learningController.getDashboard(principal);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("Unauthenticated request to dashboard throws AccessDeniedException")
    void testUnauthenticatedDashboardDenied() {
        assertThatThrownBy(() -> learningController.getDashboard(null))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Authentication and tenant context required");
    }

    @Test
    @DisplayName("Request with missing tenant ID throws AccessDeniedException")
    void testMissingTenantThrowsAccessDenied() {
        UserPrincipal principalNoTenant = new UserPrincipal(
                userId, "test@example.com", "Test", null, "default", UUID.randomUUID(), Set.of("ROLE_STUDENT"), Set.of()
        );
        assertThatThrownBy(() -> learningController.getDashboard(principalNoTenant))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("tenant context required");
    }
}

