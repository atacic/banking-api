package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransactionRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.TransactionStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionStatusServiceIT extends IntegrationTestBase {

    @Autowired
    private TransactionStatusService transactionStatusService;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private Account account;

    @BeforeEach
    void setup() {
        transactionRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User(
                LocalDateTime.now(),
                null,
                "status@test.com",
                "password",
                "Status User",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );
        user = userRepository.save(user);

        account = new Account();
        account.setAccountNumber("ACC-STATUS");
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);
        account = accountRepository.save(account);
    }

    @Test
    void shouldCreatePendingTransactionSuccessfully() {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(
                account.getId(),
                TransactionType.DEPOSIT,
                BigDecimal.valueOf(100),
                "Initial deposit"
        );

        // When
        Transaction transaction = transactionStatusService.createPendingTransaction(request, account);

        // Then
        assertThat(transaction.getId()).isNotNull();
        assertThat(transaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(transaction.getAccount().getId()).isEqualTo(account.getId());

        Transaction persistedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
        assertThat(persistedTransaction.getStatus()).isEqualTo(TransactionStatus.PENDING);
        assertThat(persistedTransaction.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    void shouldMarkTransactionAsFailedSuccessfully() {

        // Given
        Transaction transaction = new Transaction();
        transaction.setAccount(account);
        transaction.setAmount(BigDecimal.TEN);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction = transactionRepository.save(transaction);

        // When
        transactionStatusService.markTransactionFailed(transaction.getId(), "Insufficient funds");

        // Then
        Transaction updatedTransaction = transactionRepository.findById(transaction.getId()).orElseThrow();
        assertThat(updatedTransaction.getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(updatedTransaction.getDescription()).isEqualTo("FAILED: Insufficient funds");
    }
}
