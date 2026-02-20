package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.mapper.TransactionMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.repoistory.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TransactionStatusService {

    private final TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transaction createPendingTransaction(String description, BigDecimal amount, TransactionType transactionType, Account account) {
        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);
        transaction.setType(transactionType);
        transaction.setAccount(account);
        transaction.setStatus(TransactionStatus.PENDING);
        return transactionRepository.save(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTransactionFailed(Long transactionId, String reason) {
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow();
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setDescription("FAILED: " + reason);
        transactionRepository.save(transaction);
    }
}
