package com.employeeportal.backend.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MINUTES = 15;

    private record Attempt(int count, Instant lockedUntil) {}

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String email) {
        Attempt a = attempts.get(email.toLowerCase());
        if (a == null || a.lockedUntil() == null) return false;
        if (Instant.now().isAfter(a.lockedUntil())) {
            attempts.remove(email.toLowerCase());
            return false;
        }
        return true;
    }

    public long minutesRemaining(String email) {
        Attempt a = attempts.get(email.toLowerCase());
        if (a == null || a.lockedUntil() == null) return 0;
        long secondsLeft = Instant.now().until(a.lockedUntil(), ChronoUnit.SECONDS);
        return Math.max(1, secondsLeft / 60 + 1);
    }

    public void recordFailure(String email) {
        String key = email.toLowerCase();
        attempts.compute(key, (k, existing) -> {
            int newCount = (existing == null ? 0 : existing.count()) + 1;
            Instant lockedUntil = newCount >= MAX_ATTEMPTS
                ? Instant.now().plusSeconds(LOCKOUT_MINUTES * 60)
                : null;
            return new Attempt(newCount, lockedUntil);
        });
    }

    public void recordSuccess(String email) {
        attempts.remove(email.toLowerCase());
    }
}
