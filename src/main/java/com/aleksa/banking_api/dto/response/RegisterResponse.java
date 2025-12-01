package com.aleksa.banking_api.dto.response;

import com.aleksa.banking_api.model.RoleName;

import java.util.Set;

public record RegisterResponse(String email, String fullName, Set<RoleName> roles) {
    public RegisterResponse(String email, String fullName, Set<RoleName> roles) {
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
    }
}
