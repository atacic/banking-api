package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.config.RedisConfig;
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
import com.aleksa.banking_api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionStatusService transactionStatusService;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper mapper;

    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_NAME_TRANSACTIONS, key = "#transactionId")
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction with id=" + transactionId + " does not exist"));

        return mapper.transactionToTransactionResponse(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        if (transactions.isEmpty()) {
            throw new NotFoundException("No transactions found for account id=" + accountId);
        }

        return mapper.transactionsToTransactionResponses(transactions);
    }

    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#request.accountId()")
    public TransactionResponse deposit(TransactionCreateRequest request) {
        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account with id: " + request.accountId() + " not found"));

        if (request.amount() != null && request.amount().signum() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        // Create transaction with PENDING status
        Transaction transaction = transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.DEPOSIT, account);

        try {

            BigDecimal newBalance;
            newBalance = account.getBalance().add(request.amount());

            account.setBalance(newBalance);
            transaction.setBalanceAfter(newBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);

            accountRepository.save(account);
            transactionRepository.save(transaction);

            return mapper.transactionToTransactionResponse(transaction);

        } catch (RuntimeException exception) {
            transactionStatusService.markTransactionFailed(transaction.getId(), exception.getMessage());
            throw exception;
        }
    }

    @Transactional // lock is released when transaction is over (commit or rollback)
    @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#request.accountId()")
    public TransactionResponse withdrawal(TransactionCreateRequest request) {

        Account account = accountRepository.findByIdWithLock(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account with id: " + request.accountId() + " not found"));

        if (request.amount() != null && request.amount().signum() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        // Create transaction with PENDING status
        Transaction transaction = transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.WITHDRAWAL, account);

        try {

            BigDecimal newBalance;

            if (account.getBalance().compareTo(request.amount()) < 0) {
                throw new BadRequestException("Insufficient funds");
            }
            newBalance = account.getBalance().subtract(request.amount());

//            simulateSlowProcessing();

            account.setBalance(newBalance);
            transaction.setBalanceAfter(newBalance);
            transaction.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);

            return mapper.transactionToTransactionResponse(transaction);

        } catch (RuntimeException exception) {
            transactionStatusService.markTransactionFailed(transaction.getId(), exception.getMessage());
            throw exception;
        }
    }

    private void simulateSlowProcessing() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_NAME_TRANSACTIONS, key = "#transactionId")
    public TransactionResponse patchTransaction(Long transactionId, TransactionPatchRequest request) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction with id: " + transactionId + " not found"));

        if (request.status() != null) {
            transaction.setStatus(request.status());
        }

        if (request.description() != null) {
            transaction.setDescription(request.description());
        }

        transaction = transactionRepository.save(transaction);
        return mapper.transactionToTransactionResponse(transaction);
    }
}
