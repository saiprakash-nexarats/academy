package com.nexarats.academy.common.security;

import java.util.UUID;

@FunctionalInterface
public interface SessionValidator {
    boolean isSessionActive(UUID sessionId);
}
