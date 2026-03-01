package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransactionService {
    TransactionResponse deposit(TransactionCreateRequest request);
    TransactionResponse withdrawal(TransactionCreateRequest request);
    TransactionResponse patchTransaction(Long transactionId, TransactionPatchRequest request);
    TransactionResponse getTransactionById(Long transactionId);
    Page<TransactionResponse> getTransactionsByAccountId(Long accountId, Pageable pageable);
}
