package com.aleksa.banking_api.dto.request;

import java.math.BigDecimal;

public record TransferCreateRequest(String fromAccountNumber,
                                    String toAccountNumber,
                                    BigDecimal amount,
                                    String description) { }
