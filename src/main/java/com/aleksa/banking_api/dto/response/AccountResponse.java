package com.aleksa.banking_api.dto.response;

import com.aleksa.banking_api.model.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountResponse(Long id, String accountNumber, String currency, BigDecimal balance, AccountStatus status, String userEmail) { }
