package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.exception.RateLimitExceededException;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.UserRateLimiterService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class UserRateLimiterServiceIT extends IntegrationTestBase {

    @Autowired
    private UserRateLimiterService rateLimiterService;

    @Autowired
    private UserRepository userRepository;

    @Value("${rate-limiter.max.attempts:3}")
    private int maxAttempts;

    @Value("${rate-limiter.time.window.size.ms:60000}")
    private long windowMs;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        User testUser = new User(
                LocalDateTime.now(),
                null,
                "rate@test.com",
                "pass",
                "Rate Limit Tester",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );
        userRepository.save(testUser);
    }

    @AfterEach
    void cleanup() {
        userRepository.deleteAll();
    }

    private void authenticateAsUser(User user) {
        var auth = new UsernamePasswordAuthenticationToken(user, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @WithMockUser(username = "rate@test.com", roles = "USER")
    void shouldAllowExactlyMaxAttemptsWithinTimeWindow() {
        for (int i = 0; i < maxAttempts; i++) {
            boolean allowed = rateLimiterService.isWithinRateLimit("ANY_OPERATION");
            assertThat(allowed).isTrue();
        }
    }

    @Test
    @WithMockUser(username = "rate@test.com", roles = "USER")
    void shouldDenyAttemptAfterExceedingMaxAttempts() {

        // Given
        for (int i = 0; i < maxAttempts; i++) {
            // Fill window up to limit
            rateLimiterService.isWithinRateLimit("TEST_OP");
        }

        // When, Then
        boolean stillAllowed = rateLimiterService.isWithinRateLimit("TEST_OP");
        assertThat(stillAllowed).isFalse();

        assertThatThrownBy(() -> rateLimiterService.validateTransferRateLimit())
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    @WithMockUser(username = "rate@test.com", roles = "USER")
    void shouldAllowNewAttemptAfterTimeWindowSlides() throws InterruptedException {

        // Given
        for (int i = 0; i < maxAttempts; i++) {
            // Fill window up to limit
            rateLimiterService.isWithinRateLimit("SLIDING");
        }

        assertThat(rateLimiterService.isWithinRateLimit("SLIDING")).isFalse();

        // When
        Thread.sleep(windowMs + 100);

        // Then
        boolean allowedAfterWait = rateLimiterService.isWithinRateLimit("SLIDING");
        assertThat(allowedAfterWait).isTrue();
    }

    @Test
    void shouldTreatDifferentUsersIndependently() {

        // Given
        User userA = userRepository.save(new User(
                LocalDateTime.now(), null, "a@limit.test", "p", "User A", LocalDateTime.now(), UserStatus.ACTIVE));
        User userB = userRepository.save(new User(
                LocalDateTime.now(), null, "b@limit.test", "p", "User B", LocalDateTime.now(), UserStatus.ACTIVE));

        authenticateAsUser(userA);
        for (int i = 0; i < maxAttempts; i++) {
            // Fill limit for user A
            rateLimiterService.isWithinRateLimit("USER_A");
        }

        // When + Then - user A should be blocked
        assertThat(rateLimiterService.isWithinRateLimit("USER_A")).isFalse();

        // When + Then - user B should still be allowed (separate bucket)
        authenticateAsUser(userB);
        assertThat(rateLimiterService.isWithinRateLimit("USER_B")).isTrue();
        assertThat(rateLimiterService.isWithinRateLimit("USER_B")).isTrue();
    }

    @Test
    @WithMockUser(username = "rate@test.com", roles = "USER")
    void concurrentCalls_shouldRespectTheRateLimit() throws Exception {

        // Given
        int threadCount = maxAttempts + 4;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        SecurityContext testContext = SecurityContextHolder.getContext();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    DelegatingSecurityContextRunnable wrapped = new DelegatingSecurityContextRunnable(
                            () -> {
                                if (rateLimiterService.isWithinRateLimit("CONCURRENT")) {
                                    successCount.incrementAndGet();
                                }
                            },
                            testContext
                    );
                    wrapped.run();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // When
        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // Then
        assertThat(successCount.get()).isEqualTo(maxAttempts);
    }

    @Test
    @WithMockUser(username = "rate@test.com", roles = "USER")
    void validateTransferRateLimit_shouldThrowExceptionWhenLimitExceeded() {

        // Given
        for (int i = 0; i < maxAttempts; i++) {
            // Fill up to the limit
            rateLimiterService.validateTransferRateLimit();
        }

        // When + Then
        assertThatThrownBy(() -> rateLimiterService.validateTransferRateLimit())
                .isInstanceOf(RateLimitExceededException.class)
                .hasMessageContaining("Rate limit exceeded");
    }
}
