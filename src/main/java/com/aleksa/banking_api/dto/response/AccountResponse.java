package com.aleksa.banking_api.dto.response;

import com.aleksa.banking_api.model.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountResponse(Long id, String accountNumber, String currency, BigDecimal balance, AccountStatus status, String userEmail) {
    public AccountResponse(Long id, String accountNumber, String currency, BigDecimal balance, AccountStatus status, String userEmail) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
        this.userEmail = userEmail;
    }
}
