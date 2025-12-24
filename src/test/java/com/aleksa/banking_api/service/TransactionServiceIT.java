package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(response.amount().intValue()).isEqualTo(BigDecimal.valueOf(100).intValue());
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
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), TransactionType.DEPOSIT, BigDecimal.valueOf(200), "Deposit");

        // When
        TransactionResponse response = transactionService.createTransaction(request);

        // Then
        assertThat(response.amount()).isEqualTo(BigDecimal.valueOf(200));
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance().intValue()).isEqualTo(BigDecimal.valueOf(1200).intValue());
    }

    @Test
    void shouldCreateWithdrawalTransactionSuccessfully() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), TransactionType.WITHDRAWAL, BigDecimal.valueOf(300), "ATM");

        // When
        TransactionResponse response = transactionService.createTransaction(request);

        // Then
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);

        Account updated = accountRepository.findById(account.getId()).orElseThrow();
        assertThat(updated.getBalance().intValue()).isEqualTo(BigDecimal.valueOf(700).intValue());
    }

    @Test
    void shouldFailCreateWhenAccountDoesNotExist() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest( 999L, TransactionType.DEPOSIT, BigDecimal.TEN, "Fail");

        // When, Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFailCreateWhenAmountIsNegativeOrZero() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), TransactionType.DEPOSIT, BigDecimal.ZERO, "Zero");

        // When, Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldFailCreateWhenInsufficientFunds() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(account.getId(), TransactionType.WITHDRAWAL, BigDecimal.valueOf(5000), "Big withdraw");

        // When, Then
        assertThatThrownBy(() -> transactionService.createTransaction(request))
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
}
