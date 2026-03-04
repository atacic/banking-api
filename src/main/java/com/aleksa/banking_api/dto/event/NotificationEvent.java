package com.aleksa.banking_api.dto.event;

import java.util.Map;

public record NotificationEvent(String recipientEmail, String subject, String message, EventType type, Map<String, Object> metadata) {
    public enum EventType {
        USER_REGISTRATION,
        TRANSACTION_DEPOSIT,
        TRANSACTION_WITHDRAWAL,
        TRANSFER_SENT,
        TRANSFER_RECEIVED
    }
}
