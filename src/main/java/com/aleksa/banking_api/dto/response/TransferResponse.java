package com.aleksa.banking_api.dto.response;

import com.aleksa.banking_api.model.enums.TransferStatus;

import java.math.BigDecimal;

public record TransferResponse(String fromAccount,
                               String toAccount,
                               BigDecimal amount,
                               BigDecimal fromBalanceAfter,
                               BigDecimal toBalanceAfter,
                               TransferStatus status) { }
