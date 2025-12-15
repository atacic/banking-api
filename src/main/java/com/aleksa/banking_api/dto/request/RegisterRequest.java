package com.aleksa.banking_api.dto.request;

import com.aleksa.banking_api.model.RoleName;

public record RegisterRequest(String email, String password, String fullName, RoleName roleName) { }
