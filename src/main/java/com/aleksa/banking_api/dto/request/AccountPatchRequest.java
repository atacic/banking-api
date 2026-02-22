package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.enums.AccountStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record AccountPatchRequest(

        @Pattern(regexp = "RSD|EUR|USD", message = "Currency must be RSD, EUR or USD")
        String currency,

        @PositiveOrZero(message = "Balance must be zero or positive")
        BigDecimal balance,

        AccountStatus status) {

        @AssertTrue(message = "Account with balance cannot be closed")
        public boolean isValidClosure() {

                // ako status nije CLOSED – nema šta da validiramo
                if (status != AccountStatus.CLOSED) {
                        return true;
                }

                // ako jeste CLOSED → balance mora biti 0
                return balance != null && balance.compareTo(BigDecimal.ZERO) == 0;
        }
}
