package com.aleksa.banking_api.dto.response;

import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        TransactionType type,
        TransactionStatus status,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Long accountId) implements CacheableResponse { }
