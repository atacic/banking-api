package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse createTransaction(TransactionCreateRequest request);
    TransactionResponse patchTransaction(Long transactionId, TransactionPatchRequest request);
    TransactionResponse getTransactionById(Long transactionId);
    List<TransactionResponse> getTransactionsByAccountId(Long accountId);
}
