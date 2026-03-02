package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.exception.RateLimitExceededException;
import com.aleksa.banking_api.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRateLimiterService {

    private final SecurityUtil securityUtil;

    @Value("${rate-limiter.max.attempts:5}")
    private int maxAttempts;

    @Value("${rate-limiter.time.window.size.ms:60000}")
    private long timeWindowSizeMs;

    private final ConcurrentHashMap<Long, UserAttemptWindow> windows = new ConcurrentHashMap<>();

    public boolean isWithinRateLimit(String operationType) {

        Long userId = securityUtil.getCurrentUserId();
        if (userId == null) {
            log.warn("Rate limit check with null userId");
            return false;
        }

        UserAttemptWindow window = windows.computeIfAbsent(userId, k -> new UserAttemptWindow());

        window.lock.lock();
        try {
            long nowMs = Instant.now().toEpochMilli();

            // Remove stale attempts
            while (!window.timestamps.isEmpty() &&
                    nowMs - window.timestamps.peekFirst() > timeWindowSizeMs) {
                window.timestamps.pollFirst();
            }

            // Check if limit is exceeded
            if (window.timestamps.size() >= maxAttempts && window.timestamps.peekFirst() != null) {
                long oldest = window.timestamps.peekFirst();
                long waitMs = timeWindowSizeMs - (nowMs - oldest);
                log.warn("Rate limit exceeded for user {} | {} | waiting ~{}s", userId, operationType, waitMs / 1000);
                return false;
            }

            // Add new attempt
            window.timestamps.addLast(nowMs);
            return true;

        } finally {
            window.lock.unlock();
        }
    }

    @Scheduled(fixedRateString = "${rate-limiter.cleanup.interval.ms:300000}")
    public void cleanupOldEntries() {

        log.info("Rate limiter cleanup started");
        AtomicInteger cleanCount = new AtomicInteger(0);

        windows.keySet().removeIf(userId -> {
            UserAttemptWindow w = windows.get(userId);
            if (w == null) return true;
            w.lock.lock();
            try {
                Long lastAttempt = w.timestamps.peekLast();
                if (w.timestamps.isEmpty() || lastAttempt == null) return true;
                long now = Instant.now().toEpochMilli();
                boolean shouldClean = now - lastAttempt > timeWindowSizeMs * 3;
                if (shouldClean) cleanCount.incrementAndGet();
                return shouldClean;
            } finally {
                w.lock.unlock();
            }
        });

        if (cleanCount.get() > 0) {
            log.info("Cleaned up {} old rate-limiter entries", cleanCount.get());
        } else {
            log.debug("No old entries to clean up");
        }
    }

    private static class UserAttemptWindow {
        final ReentrantLock lock = new ReentrantLock();
        final Deque<Long> timestamps = new ArrayDeque<>();
    }

    public void validateTransferRateLimit() {
        if(!isWithinRateLimit("TRANSFER")) {
            throw new RateLimitExceededException("Rate limit exceeded");
        }
    }
}
