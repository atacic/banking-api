package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.enums.AccountStatus;

import java.math.BigDecimal;

public record AccountPatchRequest(String accountNumber, String currency, BigDecimal balance, AccountStatus status) { }
