package com.aleksa.banking_api.model;

import org.springframework.security.core.GrantedAuthority;

public record Authority (String authority) implements GrantedAuthority {

    public Authority (String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }
}
