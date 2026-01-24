package com.aleksa.banking_api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransferCreateRequest(
        @NotBlank(message = "From account number is required")
        String fromAccountNumber,
        @NotBlank(message = "To account number is required")
        String toAccountNumber,
        @Positive(message = "Amount must positive")
        BigDecimal amount,
        @NotBlank(message = "Description is required")
        String description) {
}
