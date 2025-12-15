package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.enums.TransactionType;

import java.math.BigDecimal;

public record TransactionCreateRequest(Long accountId, TransactionType type, BigDecimal amount, String description) { }
