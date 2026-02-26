package com.aleksa.banking_api.service.unit;

import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.exception.BadRequestException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.TransferMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.Transfer;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.model.enums.TransferStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransactionRepository;
import com.aleksa.banking_api.repoistory.TransferRepository;
import com.aleksa.banking_api.service.impl.AccountCacheService;
import com.aleksa.banking_api.service.impl.TransferServiceImpl;
import com.aleksa.banking_api.service.impl.TransferStatusService;
import com.aleksa.banking_api.service.impl.TransactionStatusService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private TransferStatusService transferStatusService;

    @Mock
    private TransactionStatusService transactionStatusService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper mapper;

    @Mock
    private AccountCacheService accountCacheService;

    @InjectMocks
    private TransferServiceImpl transferService;

    @Test
    void shouldCreateTransferSuccessfully() {

        // Given
        String fromAccountNumber = "ACC1";
        String toAccountNumber = "ACC2";
        BigDecimal amount = BigDecimal.valueOf(100);
        TransferCreateRequest request = new TransferCreateRequest(fromAccountNumber, toAccountNumber, amount, "Test transfer");

        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setAccountNumber(fromAccountNumber);
        fromAccount.setCurrency("EUR");
        fromAccount.setBalance(BigDecimal.valueOf(200));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber(toAccountNumber);
        toAccount.setCurrency("EUR");
        toAccount.setBalance(BigDecimal.valueOf(50));

        List<Account> accounts = List.of(fromAccount, toAccount);

        Transfer transfer = new Transfer();
        transfer.setId(10L);
        transfer.setStatus(TransferStatus.PENDING);

        Transaction transactionOut = new Transaction();
        transactionOut.setId(100L);

        Transaction transactionIn = new Transaction();
        transactionIn.setId(101L);

        TransferResponse response = new TransferResponse(
                fromAccountNumber,
                toAccountNumber,
                amount,
                fromAccount.getBalance().subtract(amount),
                toAccount.getBalance().add(amount),
                TransferStatus.COMPLETED
        );

        when(accountRepository.findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber)).thenReturn(accounts);
        when(transferStatusService.createPendingTransfer(request.description(), request.amount(), fromAccount, toAccount))
                .thenReturn(transfer);
        when(transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.TRANSFER_OUT, fromAccount))
                .thenReturn(transactionOut);
        when(transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.TRANSFER_IN, toAccount))
                .thenReturn(transactionIn);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(mapper.transferToTransferResponse(transfer)).thenReturn(response);

        // When
        TransferResponse result = transferService.createTransfer(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.amount()).isEqualByComparingTo(amount);
        assertThat(result.status()).isEqualTo(TransferStatus.COMPLETED);

        verify(accountRepository).findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber);
        verify(transferStatusService).createPendingTransfer(request.description(), request.amount(), fromAccount, toAccount);
        verify(transactionStatusService, times(2)).createPendingTransaction(any(), any(), any(), any());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(accountRepository).save(fromAccount);
        verify(accountRepository).save(toAccount);
        verify(transferRepository).save(transfer);
        verify(accountCacheService).evictTwoAccounts(fromAccount.getId(), toAccount.getId());
    }

    @Test
    void shouldThrowNotFoundWhenSourceAccountMissing() {

        // Given
        String fromAccountNumber = "ACC1";
        String toAccountNumber = "ACC2";
        TransferCreateRequest request = new TransferCreateRequest(fromAccountNumber, toAccountNumber, BigDecimal.TEN, "Test transfer");

        Account toAccount = new Account();
        toAccount.setAccountNumber(toAccountNumber);

        when(accountRepository.findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber))
                .thenReturn(List.of(toAccount));

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowNotFoundWhenTargetAccountMissing() {

        // Given
        String fromAccountNumber = "ACC1";
        String toAccountNumber = "ACC2";
        TransferCreateRequest request = new TransferCreateRequest(fromAccountNumber, toAccountNumber, BigDecimal.TEN, "Test transfer");

        Account fromAccount = new Account();
        fromAccount.setAccountNumber(fromAccountNumber);

        when(accountRepository.findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber))
                .thenReturn(List.of(fromAccount));

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowBadRequestWhenCurrencyMismatch() {

        // Given
        String fromAccountNumber = "ACC1";
        String toAccountNumber = "ACC2";
        TransferCreateRequest request = new TransferCreateRequest(fromAccountNumber, toAccountNumber, BigDecimal.TEN, "Test transfer");

        Account fromAccount = new Account();
        fromAccount.setAccountNumber(fromAccountNumber);
        fromAccount.setCurrency("EUR");

        Account toAccount = new Account();
        toAccount.setAccountNumber(toAccountNumber);
        toAccount.setCurrency("USD");

        when(accountRepository.findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber))
                .thenReturn(List.of(fromAccount, toAccount));

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldThrowBadRequestWhenInsufficientFunds() {

        // Given
        String fromAccountNumber = "ACC1";
        String toAccountNumber = "ACC2";
        BigDecimal amount = BigDecimal.valueOf(100);
        TransferCreateRequest request = new TransferCreateRequest(fromAccountNumber, toAccountNumber, amount, "Test transfer");

        Account fromAccount = new Account();
        fromAccount.setAccountNumber(fromAccountNumber);
        fromAccount.setCurrency("EUR");
        fromAccount.setBalance(BigDecimal.valueOf(50));

        Account toAccount = new Account();
        toAccount.setAccountNumber(toAccountNumber);
        toAccount.setCurrency("EUR");
        toAccount.setBalance(BigDecimal.valueOf(50));

        when(accountRepository.findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber))
                .thenReturn(List.of(fromAccount, toAccount));

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void shouldMarkTransferAndTransactionsFailedWhenExceptionOccurs() {

        // Given
        String fromAccountNumber = "ACC1";
        String toAccountNumber = "ACC2";
        BigDecimal amount = BigDecimal.valueOf(100);
        TransferCreateRequest request = new TransferCreateRequest(fromAccountNumber, toAccountNumber, amount, "Test transfer");

        Account fromAccount = new Account();
        fromAccount.setId(1L);
        fromAccount.setAccountNumber(fromAccountNumber);
        fromAccount.setCurrency("EUR");
        fromAccount.setBalance(BigDecimal.valueOf(200));

        Account toAccount = new Account();
        toAccount.setId(2L);
        toAccount.setAccountNumber(toAccountNumber);
        toAccount.setCurrency("EUR");
        toAccount.setBalance(BigDecimal.valueOf(50));

        List<Account> accounts = List.of(fromAccount, toAccount);

        Transfer transfer = new Transfer();
        transfer.setId(10L);

        Transaction transactionOut = new Transaction();
        transactionOut.setId(100L);

        Transaction transactionIn = new Transaction();
        transactionIn.setId(101L);

        when(accountRepository.findBothByAccountNumberWithLock(fromAccountNumber, toAccountNumber)).thenReturn(accounts);
        when(transferStatusService.createPendingTransfer(request.description(), request.amount(), fromAccount, toAccount))
                .thenReturn(transfer);
        when(transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.TRANSFER_OUT, fromAccount))
                .thenReturn(transactionOut);
        when(transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.TRANSFER_IN, toAccount))
                .thenReturn(transactionIn);
        when(transactionRepository.save(any(Transaction.class))).thenThrow(new RuntimeException("DB error"));

        // When & Then
        assertThatThrownBy(() -> transferService.createTransfer(request))
                .isInstanceOf(RuntimeException.class);

        verify(transactionStatusService).markTransactionFailed(eq(transactionOut.getId()), anyString());
        verify(transactionStatusService).markTransactionFailed(eq(transactionIn.getId()), anyString());
        verify(transferStatusService).markTransferFailed(eq(transfer.getId()), anyString());
    }
}

