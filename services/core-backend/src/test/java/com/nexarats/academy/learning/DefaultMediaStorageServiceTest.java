package com.nexarats.academy.learning;

import com.nexarats.academy.learning.service.DefaultMediaStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.io.Resource;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultMediaStorageServiceTest {

    @Mock
    private Environment environment;

    private DefaultMediaStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new DefaultMediaStorageService(environment);
    }

    @Test
    @DisplayName("Directory traversal sequences with .. are strictly rejected with 400 Bad Request")
    void testDirectoryTraversalRejected() {
        assertThatThrownBy(() -> storageService.resolveMediaResource("LOCAL_STORAGE", "../../etc/passwd"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("directory traversal");
    }

    @Test
    @DisplayName("Absolute filesystem paths starting with slash are rejected with 400 Bad Request")
    void testAbsolutePathRejected() {
        assertThatThrownBy(() -> storageService.resolveMediaResource("LOCAL_STORAGE", "/var/log/system.log"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("absolute paths are prohibited");
    }

    @Test
    @DisplayName("Windows drive paths are rejected with 400 Bad Request")
    void testWindowsDrivePathRejected() {
        assertThatThrownBy(() -> storageService.resolveMediaResource("LOCAL_STORAGE", "C:\\Windows\\System32\\cmd.exe"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("absolute paths are prohibited");
    }

    @Test
    @DisplayName("Unimplemented cloud providers (S3, MINIO) return 501 Not Implemented")
    void testCloudStorageProvidersReturnNotImplemented() {
        assertThatThrownBy(() -> storageService.resolveMediaResource("S3", "videos/lecture1.mp4"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not yet implemented");

        assertThatThrownBy(() -> storageService.resolveMediaResource("MINIO", "videos/lecture1.mp4"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("not yet implemented");
    }

    @Test
    @DisplayName("Demo media fallback is disabled by default and returns null")
    void testDemoFallbackDisabledByDefault() {
        Resource resource = storageService.resolveMediaResource("INTERNAL_STREAMING", "media/non_existent.mp4");
        assertThat(resource).isNull();
    }
}

