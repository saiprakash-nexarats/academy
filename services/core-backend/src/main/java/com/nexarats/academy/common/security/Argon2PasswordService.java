package com.nexarats.academy.common.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Enterprise Password Hashing Service using Argon2id.
 *
 * Implements current OWASP Password Storage Cheat Sheet recommendations:
 * - Algorithm: Argon2id (hybrid data-dependent and data-independent memory access)
 * - Memory cost: 16 MB (16,384 KiB) for containerized resource efficiency; scalable to 64 MB
 * - Iterations (Time cost): 3 iterations
 * - Parallelism: 1 lane
 * - Salt length: 16 bytes
 * - Hash length: 32 bytes
 *
 * Plaintext passwords are never logged or stored.
 */
@Service
@Primary
public class Argon2PasswordService implements PasswordEncoder {

    private final Argon2PasswordEncoder encoder;

    public Argon2PasswordService(
            @Value("${nexarats.auth.argon2.salt-length:16}") int saltLength,
            @Value("${nexarats.auth.argon2.hash-length:32}") int hashLength,
            @Value("${nexarats.auth.argon2.parallelism:1}") int parallelism,
            @Value("${nexarats.auth.argon2.memory-cost:16384}") int memoryCost,
            @Value("${nexarats.auth.argon2.time-cost:3}") int iterations
    ) {
        this.encoder = new Argon2PasswordEncoder(
                saltLength,
                hashLength,
                parallelism,
                memoryCost,
                iterations
        );
    }

    @Override
    public String encode(CharSequence rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return encoder.upgradeEncoding(encodedPassword);
    }

    /**
     * Validates candidate password against enterprise security policy.
     * Allows password-manager generated passwords (min 8 chars, max 128 chars).
     */
    public void validatePasswordPolicy(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters in length");
        }
        if (password.length() > 128) {
            throw new IllegalArgumentException("Password cannot exceed 128 characters in length");
        }
    }
}
