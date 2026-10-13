package com.nexarats.academy.identity.service;

import java.util.List;

/**
 * Enterprise Email Delivery Abstraction.
 * In development, captures messages into memory for verification.
 * In production, configured with external SMTP / transactional provider.
 */
public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken, String firstName);
    void sendEmailVerification(String toEmail, String verificationToken, String firstName);
}
