package com.aleksa.banking_api.exception.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForbiddenExceptionResponse {
    String message;
}
