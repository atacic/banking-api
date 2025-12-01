package com.aleksa.banking_api.exception.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UnauthorizedResponse {
    String message;

    public UnauthorizedResponse(String message) {
        this.message = message;
    }
}
