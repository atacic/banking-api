package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.enums.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionCreateRequest(
        @NotNull(message = "Account id is required")
        Long accountId,
        TransactionType type,
        @Positive(message = "Amount must positive")
        BigDecimal amount,
        @NotBlank(message = "Description is required")
        String description) {
}
