package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.enums.TransactionStatus;

public record TransactionPatchRequest(TransactionStatus status, String description) { }
