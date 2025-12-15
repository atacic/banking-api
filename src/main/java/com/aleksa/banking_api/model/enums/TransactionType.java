package com.aleksa.banking_api.model.enums;

public enum TransactionType {
    DEPOSIT,       // Money added to the account directly (e.g., cash deposit)
    WITHDRAWAL,    // Money withdrawn (e.g., ATM, cash out)
    TRANSFER_IN,   // Money received from another account
    TRANSFER_OUT   // Money sent to another account
}
