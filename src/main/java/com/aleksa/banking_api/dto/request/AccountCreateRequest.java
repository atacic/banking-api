package com.aleksa.banking_api.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AccountCreateRequest(

        @NotBlank(message = "Currency is required")
        @Pattern(
                regexp = "RSD|EUR|USD",
                message = "Currency must be RSD, EUR or USD"
        )
        String currency,

        @NotNull(message = "Balance is required")
        @PositiveOrZero(message = "Balance must be zero or positive")
        BigDecimal balance,

        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        String userEmail) {
}
