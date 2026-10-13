package com.nexarats.academy.admissions.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitApplicationRequest(
        @NotBlank(message = "Full name is required")
        @Size(min = 2, max = 255, message = "Full name must be between 2 and 255 characters")
        String fullName,

        @NotBlank(message = "Email is required")
        @Email(message = "A valid email address is required")
        String email,

        @NotBlank(message = "Phone number is required")
        @Size(min = 7, max = 64, message = "Phone number must be at least 7 characters")
        String phone,

        String targetProgramOrCourse,

        String experienceLevel,

        String statementOfPurpose
) {}
