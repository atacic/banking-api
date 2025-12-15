package com.aleksa.banking_api.dto.request;

import java.math.BigDecimal;

public record AccountCreateRequest(String accountNumber, String currency, BigDecimal balance, String userEmail) { }
