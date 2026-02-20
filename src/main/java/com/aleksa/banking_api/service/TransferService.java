package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransferResponse;

public interface TransferService {
    TransferResponse createTransfer(TransferCreateRequest request);
}
