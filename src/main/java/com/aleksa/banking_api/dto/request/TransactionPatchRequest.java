package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.enums.TransactionStatus;
import jakarta.validation.constraints.AssertTrue;

public record TransactionPatchRequest(
        TransactionStatus status,
        String description) {

    @AssertTrue(message = "Transaction patch is not valid. All fields are null")
    public boolean isValidPatch() {
        if (status == null && description == null) {
            return false;
        } {
         return true;
        }
    }
}
