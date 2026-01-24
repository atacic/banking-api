package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "User email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "User password is required")
        String password,
        @NotBlank(message = "User full name is required")
        String fullName,
        RoleName roleName) {
}
