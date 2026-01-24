package com.aleksa.banking_api.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "User password is required")
        String password) {
}
