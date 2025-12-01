package com.aleksa.banking_api.dto.response;

public record LoginResponse(String token, boolean success) {
    public LoginResponse(String token, boolean success) {
        this.token = token;
        this.success = success;
    }
}
