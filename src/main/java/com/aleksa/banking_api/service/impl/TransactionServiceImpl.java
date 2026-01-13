package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.dto.response.TransferResponse;
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

    @Transactional
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException("Transaction with id=" + transactionId + " does not exist"));

        return mapper.transactionToTransactionResponse(transaction);
    }

    @Transactional
    public List<TransactionResponse> getTransactionsByAccountId(Long accountId) {
        List<Transaction> transactions = transactionRepository.findByAccountId(accountId);

        if (transactions.isEmpty()) {
            throw new NotFoundException("No transactions found for account id=" + accountId);
        }

        return mapper.transactionsToTransactionResponses(transactions);
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionCreateRequest request) {

        Account account = accountRepository.findById(request.accountId())
                .orElseThrow(() -> new NotFoundException("Account with id: " + request.accountId() + " not found"));

        if (request.amount() != null && request.amount().signum() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        // Create transaction with PENDING status
        Transaction transaction = transactionStatusService.createPendingTransaction(request, account);

        try {
            // Calculate new balance
            BigDecimal newBalance;

            switch (request.type()) {
                case DEPOSIT -> {
                    newBalance = account.getBalance().add(request.amount());
                }
                case WITHDRAWAL -> {
                    if (account.getBalance().compareTo(request.amount()) < 0) {
                        throw new BadRequestException("Insufficient funds");
                    }
                    newBalance = account.getBalance().subtract(request.amount());
                }
                default -> throw new BadRequestException("Unsupported transaction type");
            }

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

    @Override
    @Transactional
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

    @Override
    @Transactional
    public TransferResponse createTransfer(TransferCreateRequest request) {
        Account fromAccount = accountRepository.findByAccountNumber(request.fromAccountNumber())
                .orElseThrow(() -> new NotFoundException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new NotFoundException("Target account not found"));

        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new BadRequestException("Insufficient funds in source account");
        }

        // Source transaction
        Transaction transactionOut = new Transaction();
        transactionOut.setAccount(fromAccount);
        transactionOut.setType(TransactionType.TRANSFER_OUT);
        transactionOut.setAmount(request.amount());
        transactionOut.setStatus(TransactionStatus.PENDING);
        transactionOut.setDescription(request.description());
        transactionOut.setBalanceAfter(fromAccount.getBalance().subtract(request.amount()));
        transactionRepository.save(transactionOut);

        // Target transaction
        Transaction transactionIn = new Transaction();
        transactionIn.setAccount(toAccount);
        transactionIn.setType(TransactionType.TRANSFER_IN);
        transactionIn.setAmount(request.amount());
        transactionIn.setStatus(TransactionStatus.PENDING);
        transactionIn.setDescription(request.description());
        transactionIn.setBalanceAfter(toAccount.getBalance().add(request.amount()));
        transactionRepository.save(transactionIn);

        // Update accounts balances
        fromAccount.setBalance(fromAccount.getBalance().subtract(request.amount()));
        toAccount.setBalance(toAccount.getBalance().add(request.amount()));
        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        // FINALIZE transactions
        transactionOut.setStatus(TransactionStatus.COMPLETED);
        transactionIn.setStatus(TransactionStatus.COMPLETED);
        transactionRepository.save(transactionOut);
        transactionRepository.save(transactionIn);

        return new TransferResponse(
                transactionOut.getId(),
                transactionIn.getId(),
                request.amount(),
                fromAccount.getBalance(),
                toAccount.getBalance(),
                TransactionStatus.COMPLETED
        );
    }
}
