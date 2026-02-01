package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.exception.BadRequestException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransactionRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

class TransactionServiceIT extends IntegrationTestBase {

    @Autowired
    private TransactionServiceImpl transactionService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private Account account;

    @BeforeEach
    void setup() {
        User user;
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
        user = new User(
                LocalDateTime.now(),
                null,
                "tx@test.com",
                "pass",
                "TX USER",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );
        user = userRepository.save(user);
        account = new Account();
        account.setAccountNumber("ACC-TX");
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);
        account = accountRepository.save(account);
    }

    @Test
    void shouldGetTransactionById() {

        // Given
        Transaction transaction = new Transaction();
        transaction.setAmount(BigDecimal.valueOf(100));
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAccount(account);
        transaction = transactionRepository.save(transaction);

        // When
        TransactionResponse response = transactionService.getTransactionById(transaction.getId());

        // Then
        assertThat(response.id()).isEqualTo(transaction.getId());
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldFailGetWhenTransactionNotFoundById() {

        // Given
        Long missingId = 999L;

        // When, Then
        assertThatThrownBy(() -> transactionService.getTransactionById(missingId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetTransactionsByAccountId() {

        // Given
        Transaction transaction1 = new Transaction();
        transaction1.setAccount(account);
        transaction1.setAmount(BigDecimal.TEN);
        transaction1.setType(TransactionType.DEPOSIT);
        transaction1.setStatus(TransactionStatus.COMPLETED);

        Transaction transaction2 = new Transaction();
        transaction2.setAccount(account);
        transaction2.setAmount(BigDecimal.ONE);
        transaction2.setType(TransactionType.WITHDRAWAL);
        transaction2.setStatus(TransactionStatus.COMPLETED);

        transactionRepository.saveAll(List.of(transaction1, transaction2));

        // When
        List<TransactionResponse> list = transactionService.getTransactionsByAccountId(account.getId());

        // Then
        assertThat(list).hasSize(2);
    }

    @Test
    void shouldFailGetWhenNoTransactionsForAccount() {

        // Given
        Long accountId = 123L;

        // When, Then
        assertThatThrownBy(() -> transactionService.getTransactionsByAccountId(accountId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldCreateDepositTransactionSuccessfully() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), BigDecimal.valueOf(200), "Deposit");

        // When
        TransactionResponse response = transactionService.deposit(request);

        // Then
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1200));
    }

    @Test
    void shouldCreateWithdrawalTransactionSuccessfully() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), BigDecimal.valueOf(300), "ATM");

        // When
        TransactionResponse response = transactionService.withdrawal(request);

        // Then
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(700));
    }

    @Test
    void shouldFailCreateWhenAccountDoesNotExist() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest( 999L, BigDecimal.TEN, "Fail");

        // When, Then
        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> transactionService.withdrawal(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFailCreateWhenAmountIsNegativeOrZero() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), BigDecimal.ZERO, "Zero");

        // When, Then
        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> transactionService.withdrawal(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldFailCreateWhenInsufficientFunds() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), BigDecimal.valueOf(5000), "Big withdraw");

        // When, Then
        assertThatThrownBy(() -> transactionService.withdrawal(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldPatchTransactionSuccessfully() {

        // Given
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setType(TransactionType.DEPOSIT);
        transaction = transactionRepository.save(transaction);

        TransactionPatchRequest request = new TransactionPatchRequest(TransactionStatus.COMPLETED, "Updated desc");

        // When
        TransactionResponse response = transactionService.patchTransaction(transaction.getId(), request);

        // Then
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(response.description()).isEqualTo("Updated desc");
    }

    @Test
    void shouldFailPatchWhenTransactionNotFound() {

        // Given
        Long missingId = 123L;
        TransactionPatchRequest request = new TransactionPatchRequest(TransactionStatus.FAILED, "Does not exist");

        // When, Then
        assertThatThrownBy(() -> transactionService.patchTransaction(missingId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldCreateTransferSuccessfully() {

        // Given
        Account targetAccount = new Account();
        targetAccount.setAccountNumber("ACC-TARGET");
        targetAccount.setCurrency("EUR");
        targetAccount.setBalance(BigDecimal.valueOf(500));
        targetAccount.setStatus(AccountStatus.ACTIVE);
        targetAccount.setUser(account.getUser());
        targetAccount = accountRepository.save(targetAccount);

        TransferCreateRequest request = new TransferCreateRequest(
                account.getAccountNumber(),
                targetAccount.getAccountNumber(),
                BigDecimal.valueOf(200),
                "Rent payment"
        );

        // When
        TransferResponse response = transactionService.createTransfer(request);

        // Then - response validation
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(response.transferredAmount()).isEqualByComparingTo("200");
        assertThat(response.fromBalanceAfter()).isEqualByComparingTo("800");
        assertThat(response.toBalanceAfter()).isEqualByComparingTo("700");
        assertThat(response.fromTransactionId()).isNotNull();
        assertThat(response.toTransactionId()).isNotNull();

        // Then - account balances in DB
        Account fromUpdated = accountRepository.findById(account.getId()).orElseThrow();
        Account toUpdated = accountRepository.findById(targetAccount.getId()).orElseThrow();
        assertThat(fromUpdated.getBalance()).isEqualByComparingTo("800");
        assertThat(toUpdated.getBalance()).isEqualByComparingTo("700");

        // Then - transactions persisted
        Transaction txOut = transactionRepository.findById(response.fromTransactionId()).orElseThrow();
        Transaction txIn = transactionRepository.findById(response.toTransactionId()).orElseThrow();
        assertThat(txOut.getType()).isEqualTo(TransactionType.TRANSFER_OUT);
        assertThat(txOut.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(txOut.getAmount()).isEqualByComparingTo("200");
        assertThat(txIn.getType()).isEqualTo(TransactionType.TRANSFER_IN);
        assertThat(txIn.getStatus()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(txIn.getAmount()).isEqualByComparingTo("200");
    }

    @Test
    void shouldFailCreateTransferWhenSourceAccountDoesNotExist() {

        // Given
        TransferCreateRequest request = new TransferCreateRequest(
                "MISSING-ACC",
                account.getAccountNumber(),
                BigDecimal.valueOf(100),
                "Invalid source"
        );

        // When, Then
        assertThatThrownBy(() -> transactionService.createTransfer(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFailCreateTransferWhenTargetAccountDoesNotExist() {

        // Given
        TransferCreateRequest request = new TransferCreateRequest(
                account.getAccountNumber(),
                "MISSING-ACC",
                BigDecimal.valueOf(100),
                "Invalid target"
        );

        // When, Then
        assertThatThrownBy(() -> transactionService.createTransfer(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFailCreateTransferWhenInsufficientFunds() {

        // Given
        Account targetAccount = new Account();
        targetAccount.setAccountNumber("ACC-TARGET-FAIL");
        targetAccount.setCurrency("EUR");
        targetAccount.setBalance(BigDecimal.valueOf(100));
        targetAccount.setStatus(AccountStatus.ACTIVE);
        targetAccount.setUser(account.getUser());
        accountRepository.save(targetAccount);

        TransferCreateRequest request = new TransferCreateRequest(
                account.getAccountNumber(),
                targetAccount.getAccountNumber(),
                BigDecimal.valueOf(5000),
                "Too much"
        );

        // When, Then
        assertThatThrownBy(() -> transactionService.createTransfer(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void concurrentDeposit_shouldThrowOptimisticLockException() throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();

        Runnable task = () -> {
            try {
                startLatch.await();
                transactionService.deposit(new TransactionCreateRequest(account.getId(), BigDecimal.valueOf(500), "Deposit"));
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        };

        new Thread(task).start();
        new Thread(task).start();

        startLatch.countDown();
        doneLatch.await();

        // Jedna transakcija mora da uspe, druga da fail-uje zbog optimistic lock
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failureCount.get()).isEqualTo(1);

        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(1500)); // 1000 + 500
    }

    @Test
    void concurrentWithdrawal_shouldBeSerialized_withPessimisticLock() throws InterruptedException {

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(2);

        Runnable task = () -> {
            try {
                startLatch.await();
                transactionService.withdrawal(new TransactionCreateRequest(account.getId(), BigDecimal.valueOf(700), "ATM"));
            } catch (Exception e) {
                // Druga transakcija će baciti BadRequestException jer nema dovoljno sredstava
                assertThat(e).isInstanceOf(BadRequestException.class);
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

        // Jedan withdrawal uspe, drugi mora da fail-uje jer nema dovoljno sredstava
        Account updatedAccount = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updatedAccount.getBalance()).isEqualByComparingTo("300");
    }
}
