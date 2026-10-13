package com.nexarats.academy.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BootstrapAdminRequest(
        @NotBlank(message = "Bootstrap secret is required")
        String bootstrapSecret,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 10, max = 128, message = "Administrator password must be at least 10 characters")
        String password,

        @NotBlank(message = "First name is required")
        String firstName,

        @NotBlank(message = "Last name is required")
        String lastName,

        @NotBlank(message = "Tenant slug is required")
        String tenantSlug,

        @NotBlank(message = "Tenant name is required")
        String tenantName
) {}
