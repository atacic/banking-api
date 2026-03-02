package com.aleksa.banking_api.service.unit;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.exception.BadRequestException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.TransactionMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransactionRepository;
import com.aleksa.banking_api.service.impl.TransactionServiceImpl;
import com.aleksa.banking_api.service.impl.TransactionStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionStatusService transactionStatusService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper mapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    @Test
    void shouldGetTransactionByIdSuccessfully() {

        // Given
        Long transactionId = 1L;
        Transaction transaction = new Transaction();
        TransactionResponse response = new TransactionResponse(
                transactionId,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.TEN,
                "Test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(mapper.transactionToTransactionResponse(transaction)).thenReturn(response);

        // When
        TransactionResponse result = transactionService.getTransactionById(transactionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(transactionId);
        verify(transactionRepository).findById(transactionId);
        verify(mapper).transactionToTransactionResponse(transaction);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenTransactionDoesNotExist() {

        // Given
        Long transactionId = 99L;
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> transactionService.getTransactionById(transactionId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldGetTransactionsByAccountIdSuccessfully() {

        // Given
        Long accountId = 1L;
        Transaction transaction = new Transaction();
        Page<Transaction> transactionsPage = new PageImpl<>(Collections.singletonList(transaction));
        TransactionResponse response = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.TEN,
                "Test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                accountId
        );
        Page<TransactionResponse> transactionResponsePage = new PageImpl<>(Collections.singletonList(response));

        Pageable pageable = PageRequest.of(0, 10);

        when(transactionRepository.findByAccountId(accountId, pageable)).thenReturn(transactionsPage);
        when(mapper.transactionsToTransactionResponses(transactionsPage)).thenReturn(transactionResponsePage);

        // When
        Page<TransactionResponse> result = transactionService.getTransactionsByAccountId(accountId, pageable);

        // Then
        assertThat(result).hasSize(1);
        verify(transactionRepository).findByAccountId(accountId, pageable);
        verify(mapper).transactionsToTransactionResponses(transactionsPage);
    }

    @Test
    void shouldReturnAllTransactionsWhenAccountIdNotProvided() {

        // Given
        Transaction transaction = new Transaction();
        Page<Transaction> transactionsPage = new PageImpl<>(Collections.singletonList(transaction));
        TransactionResponse response = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.TEN,
                "Test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );
        Page<TransactionResponse> transactionResponsePage = new PageImpl<>(Collections.singletonList(response));

        Pageable pageable = PageRequest.of(0, 10);

        when(transactionRepository.findAll(pageable)).thenReturn(transactionsPage);
        when(mapper.transactionsToTransactionResponses(transactionsPage)).thenReturn(transactionResponsePage);

        // When
        Page<TransactionResponse> result = transactionService.getTransactionsByAccountId(null, pageable);

        // Then
        assertThat(result).hasSize(1);
        verify(transactionRepository).findAll(pageable);
        verify(mapper).transactionsToTransactionResponses(transactionsPage);
    }

    @Test
    void shouldDepositSuccessfully() {

        // Given
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionCreateRequest request = new TransactionCreateRequest(accountId, amount, "Deposit");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.ZERO);

        Transaction transaction = new Transaction();
        transaction.setId(10L);

        TransactionResponse response = new TransactionResponse(
                transaction.getId(),
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                amount,
                amount,
                "Deposit",
                LocalDateTime.now(),
                LocalDateTime.now(),
                accountId
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.DEPOSIT, account))
                .thenReturn(transaction);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(transaction);
        when(accountRepository.save(account)).thenReturn(account);
        when(mapper.transactionToTransactionResponse(transaction)).thenReturn(response);

        // When
        TransactionResponse result = transactionService.deposit(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(amount);
        verify(accountRepository).findById(accountId);
        verify(transactionStatusService).createPendingTransaction(request.description(), request.amount(), TransactionType.DEPOSIT, account);
        verify(accountRepository).save(account);
        verify(transactionRepository).save(transaction);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenDepositAmountNotPositive() {

        // Given
        Long accountId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(accountId, BigDecimal.ZERO, "Deposit");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.TEN);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> transactionService.deposit(request))
                .isInstanceOf(BadRequestException.class);

        verify(accountRepository).findById(accountId);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(transactionStatusService, transactionRepository);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenWithdrawalAmountNotPositive() {

        // Given
        Long accountId = 1L;
        TransactionCreateRequest request = new TransactionCreateRequest(accountId, BigDecimal.ZERO, "Withdrawal");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.TEN);

        when(accountRepository.findByIdWithLock(accountId)).thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> transactionService.withdrawal(request))
                .isInstanceOf(BadRequestException.class);

        verify(accountRepository).findByIdWithLock(accountId);
        verifyNoMoreInteractions(accountRepository);
        verifyNoInteractions(transactionStatusService, transactionRepository);
    }

    @Test
    void shouldThrowBadRequestExceptionWhenInsufficientFundsOnWithdrawal() {

        // Given
        Long accountId = 1L;
        BigDecimal amount = BigDecimal.valueOf(100);
        TransactionCreateRequest request = new TransactionCreateRequest(accountId, amount, "Withdrawal");

        Account account = new Account();
        account.setId(accountId);
        account.setBalance(BigDecimal.valueOf(50));

        when(accountRepository.findByIdWithLock(accountId)).thenReturn(Optional.of(account));
        Transaction transaction = new Transaction();
        transaction.setId(10L);
        when(transactionStatusService.createPendingTransaction(
                request.description(),
                request.amount(),
                TransactionType.WITHDRAWAL,
                account
        )).thenReturn(transaction);

        // When & Then
        assertThatThrownBy(() -> transactionService.withdrawal(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Insufficient funds");

        verify(accountRepository).findByIdWithLock(accountId);
        verify(transactionStatusService).createPendingTransaction(
                request.description(),
                request.amount(),
                TransactionType.WITHDRAWAL,
                account
        );
        verify(transactionStatusService).markTransactionFailed(transaction.getId(), "Insufficient funds");
        verifyNoInteractions(transactionRepository);
    }

    @Test
    void shouldPatchTransactionSuccessfully() {

        // Given
        Long transactionId = 1L;
        TransactionPatchRequest request = new TransactionPatchRequest(TransactionStatus.COMPLETED, "Updated");

        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription("Old");

        Transaction updated = new Transaction();
        updated.setId(transactionId);
        updated.setStatus(TransactionStatus.COMPLETED);
        updated.setDescription("Updated");

        TransactionResponse response = new TransactionResponse(
                transactionId,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.TEN,
                "Updated",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(transaction)).thenReturn(updated);
        when(mapper.transactionToTransactionResponse(updated)).thenReturn(response);

        // When
        TransactionResponse result = transactionService.patchTransaction(transactionId, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(result.description()).isEqualTo("Updated");
        verify(transactionRepository).findById(transactionId);
        verify(transactionRepository).save(transaction);
        verify(mapper).transactionToTransactionResponse(updated);
    }
}

