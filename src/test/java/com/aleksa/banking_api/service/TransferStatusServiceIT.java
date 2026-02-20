package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transfer;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.*;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransferRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.TransferStatusService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class TransferStatusServiceIT extends IntegrationTestBase {

    @Autowired
    private TransferStatusService transferStatusService;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private Account fromAccount;
    private Account toAccount;

    @BeforeEach
    void setup() {
        transferRepository.deleteAll();
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

        fromAccount = new Account();
        fromAccount.setAccountNumber("ACC-111111");
        fromAccount.setCurrency("EUR");
        fromAccount.setBalance(BigDecimal.valueOf(1000));
        fromAccount.setStatus(AccountStatus.ACTIVE);
        fromAccount.setUser(user);
        fromAccount = accountRepository.save(fromAccount);

        toAccount = new Account();
        toAccount.setAccountNumber("ACC-111111");
        toAccount.setCurrency("EUR");
        toAccount.setBalance(BigDecimal.valueOf(1000));
        toAccount.setStatus(AccountStatus.ACTIVE);
        toAccount.setUser(user);
        toAccount = accountRepository.save(fromAccount);
    }

    @AfterEach
    void clean() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreatePendingTransferSuccessfully() {

        // Given
        String description = "transfer";
        BigDecimal amount = BigDecimal.valueOf(100);

        // When
        Transfer transfer = transferStatusService.createPendingTransfer(description, amount, fromAccount, toAccount);

        // Then
        assertThat(transfer.getId()).isNotNull();
        assertThat(transfer.getStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(transfer.getDescription()).isEqualTo(description);
        assertThat(transfer.getAmount()).isEqualByComparingTo(amount);
        assertThat(transfer.getFromAccount().getId()).isEqualTo(fromAccount.getId());
        assertThat(transfer.getToAccount().getId()).isEqualTo(toAccount.getId());

        Transfer persistedTransfer = transferRepository.findById(transfer.getId()).orElseThrow();
        assertThat(persistedTransfer.getId()).isNotNull();
        assertThat(persistedTransfer.getStatus()).isEqualTo(TransferStatus.PENDING);
        assertThat(persistedTransfer.getDescription()).isEqualTo(description);
        assertThat(persistedTransfer.getAmount()).isEqualByComparingTo(amount);
        assertThat(persistedTransfer.getFromAccount().getId()).isEqualTo(fromAccount.getId());
        assertThat(persistedTransfer.getToAccount().getId()).isEqualTo(toAccount.getId());
    }

    @Test
    void shouldMarkTransactionAsFailedSuccessfully() {

        // Given
        Transfer transfer = new Transfer();
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setDescription("transfer description");
        transfer.setAmount(BigDecimal.TEN);
        transfer = transferRepository.save(transfer);

        // When
        transferStatusService.markTransferFailed(transfer.getId(), "Insufficient funds");

        // Then
        Transfer updatedTransfer = transferRepository.findById(transfer.getId()).orElseThrow();
        assertThat(updatedTransfer.getStatus()).isEqualTo(TransferStatus.FAILED);
        assertThat(updatedTransfer.getDescription()).isEqualTo("FAILED: Insufficient funds");
    }
}
