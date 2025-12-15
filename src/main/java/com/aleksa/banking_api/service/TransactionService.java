package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse createTransaction(TransactionCreateRequest request);

    TransactionResponse patchTransaction(Long transactionId, TransactionPatchRequest request);
}
