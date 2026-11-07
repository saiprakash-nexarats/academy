package com.nexarats.academy.onboarding.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 255, message = "Headline must not exceed 255 characters")
        String headline,

        @Size(max = 2000, message = "Bio must not exceed 2000 characters")
        String bio,

        @Size(max = 2000, message = "Academic background must not exceed 2000 characters")
        String academicBackground,

        @Size(max = 2000, message = "Career goals must not exceed 2000 characters")
        String careerGoals,

        @Size(max = 64, message = "Timezone must not exceed 64 characters")
        @Pattern(regexp = "^$|^[A-Za-z0-9_+\\-/]+$", message = "Invalid timezone format")
        String timezone,

        @Pattern(
                regexp = "^$|^(https?://)?(www\\.)?github\\.com/[A-Za-z0-9_.-]+/?$",
                message = "Invalid GitHub profile URL. Must be in format https://github.com/username"
        )
        String githubUrl,

        @Pattern(
                regexp = "^$|^(https?://)?(www\\.)?linkedin\\.com/in/[A-Za-z0-9_.-]+/?$",
                message = "Invalid LinkedIn profile URL. Must be in format https://linkedin.com/in/username"
        )
        String linkedinUrl
) {
}
