package com.aleksa.banking_api.dto.request;

public record LoginRequest(String email, String password) {
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}
