package com.nexarats.academy.identity.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Development & Testing Email Service.
 * Captures email dispatch in a thread-safe list for automated test validation.
 * Redacts secrets from application logs.
 */
@Service
public class DevEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(DevEmailService.class);

    public record SentEmail(
            String toEmail,
            String subject,
            String token,
            Instant timestamp
    ) {}

    private final List<SentEmail> sentEmails = new CopyOnWriteArrayList<>();

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken, String firstName) {
        log.info("Sending password reset email to '{}' (name: '{}') [DEV MODE: token generated]", toEmail, firstName);
        sentEmails.add(new SentEmail(toEmail, "Password Reset Request", resetToken, Instant.now()));
    }

    @Override
    public void sendEmailVerification(String toEmail, String verificationToken, String firstName) {
        log.info("Sending email verification to '{}' (name: '{}') [DEV MODE: token generated]", toEmail, firstName);
        sentEmails.add(new SentEmail(toEmail, "Verify Your Email Address", verificationToken, Instant.now()));
    }

    public List<SentEmail> getSentEmails() {
        return sentEmails;
    }

    public void clear() {
        sentEmails.clear();
    }
}
