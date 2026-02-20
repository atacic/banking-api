package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.exception.BadRequestException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.*;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransferRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.TransferServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransferServiceIT extends IntegrationTestBase {

    @Autowired
    private TransferServiceImpl transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransferRepository transferRepository;

    private Account sourceAccount;
    private User testUser;

    @BeforeEach
    void setup() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User(
                LocalDateTime.now(),
                null,
                "transfer@test.com",
                "pass",
                "TRANSFER TEST USER",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );
        testUser = userRepository.save(testUser);

        sourceAccount = new Account();
        sourceAccount.setAccountNumber("ACC-12345");
        sourceAccount.setCurrency("EUR");
        sourceAccount.setBalance(BigDecimal.valueOf(2000));
        sourceAccount.setStatus(AccountStatus.ACTIVE);
        sourceAccount.setUser(testUser);
        sourceAccount = accountRepository.save(sourceAccount);
    }

    @AfterEach
    void clean() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateTransferSuccessfully() {

        // Given
        Account targetAccount = new Account();
        targetAccount.setAccountNumber("ACC-98765");
        targetAccount.setCurrency("EUR");
        targetAccount.setBalance(BigDecimal.valueOf(800));
        targetAccount.setStatus(AccountStatus.ACTIVE);
        targetAccount.setUser(testUser);
        targetAccount = accountRepository.save(targetAccount);

        TransferCreateRequest request = new TransferCreateRequest(
                sourceAccount.getAccountNumber(),
                targetAccount.getAccountNumber(),
                BigDecimal.valueOf(350),
                "February rent"
        );

        // When
        TransferResponse response = transferService.createTransfer(request);

        // Then - response
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(350));
        assertThat(response.fromAccount()).isEqualTo("ACC-12345");
        assertThat(response.toAccount()).isEqualTo("ACC-98765");
        assertThat(response.fromBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(1650));
        assertThat(response.toBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(1150));

        // Then - database state
        Account updatedSource = accountRepository.findById(sourceAccount.getId()).orElseThrow();
        Account updatedTarget = accountRepository.findById(targetAccount.getId()).orElseThrow();

        assertThat(updatedSource.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1650));
        assertThat(updatedTarget.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1150));

        // Check that Transfer entity exists and is COMPLETED
        assertThat(transferRepository.findAll()).hasSize(1);
        assertThat(transferRepository.findAll().getFirst().getStatus()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void shouldFailCreateTransferWhenSourceAccountDoesNotExist() {

        // Given
        TransferCreateRequest request = new TransferCreateRequest(
                "NON-EXISTENT-ACC",
                sourceAccount.getAccountNumber(),
                BigDecimal.valueOf(100),
                "Invalid source"
        );

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Source account not found");
    }

    @Test
    void shouldFailCreateTransferWhenTargetAccountDoesNotExist() {

        // Given
        TransferCreateRequest request = new TransferCreateRequest(
                sourceAccount.getAccountNumber(),
                "NON-EXISTENT-ACC",
                BigDecimal.valueOf(200),
                "Invalid target"
        );

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Target account not found");
    }

    @Test
    void shouldFailCreateTransferWhenInsufficientFunds() {

        // Given
        Account targetAccount = new Account();
        targetAccount.setAccountNumber("ACC-LOW");
        targetAccount.setCurrency("EUR");
        targetAccount.setBalance(BigDecimal.valueOf(300));
        targetAccount.setStatus(AccountStatus.ACTIVE);
        targetAccount.setUser(testUser);
        accountRepository.save(targetAccount);

        TransferCreateRequest request = new TransferCreateRequest(
                sourceAccount.getAccountNumber(),
                targetAccount.getAccountNumber(),
                BigDecimal.valueOf(3000),   // > 2000
                "Too expensive gift"
        );

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient funds");
    }

    @Test
    void concurrentTransfers_shouldSerializeWithPessimisticLock() throws InterruptedException {

        // Given
        Account targetAccountToSave = new Account();
        targetAccountToSave.setAccountNumber("ACC-CONCURRENT");
        targetAccountToSave.setCurrency("EUR");
        targetAccountToSave.setBalance(BigDecimal.valueOf(100));
        targetAccountToSave.setStatus(AccountStatus.ACTIVE);
        targetAccountToSave.setUser(testUser);
        Account targetAccount = accountRepository.save(targetAccountToSave);

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Runnable task = () -> {
            try {
                startLatch.await();
                transferService.createTransfer(new TransferCreateRequest(
                        sourceAccount.getAccountNumber(),
                        targetAccount.getAccountNumber(),
                        BigDecimal.valueOf(1200),
                        "Concurrent test"
                ));
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();

        startLatch.countDown();
        doneLatch.await();

        // Expect: only one succeeds (due to balance check + lock), second fails on insufficient funds
        assertThat(successCount.get() + failureCount.get()).isEqualTo(2);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);

        Account updatedSource = accountRepository.findById(sourceAccount.getId()).orElseThrow();
        assertThat(updatedSource.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(800)); // 2000 - 1200

        Account updatedTarget = accountRepository.findById(targetAccount.getId()).orElseThrow();
        assertThat(updatedTarget.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1300)); // 100 + 1200
    }
}
