package com.aleksa.banking_api.dto.response;

import com.aleksa.banking_api.model.enums.TransactionStatus;

import java.math.BigDecimal;

public record TransferResponse(Long fromTransactionId,
                               Long toTransactionId,
                               BigDecimal transferredAmount,
                               BigDecimal fromBalanceAfter,
                               BigDecimal toBalanceAfter,
                               TransactionStatus status) { }
