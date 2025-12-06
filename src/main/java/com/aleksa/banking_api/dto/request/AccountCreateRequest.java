package com.aleksa.banking_api.dto.request;

import java.math.BigDecimal;

public record AccountCreateRequest(String accountNumber, String currency, BigDecimal balance, String userEmail) {
    public AccountCreateRequest(String accountNumber, String currency, BigDecimal balance, String userEmail) {
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
        this.userEmail = userEmail;
    }
}
