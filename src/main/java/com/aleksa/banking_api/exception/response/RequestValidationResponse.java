package com.aleksa.banking_api.exception.response;

import java.util.Map;

public record RequestValidationResponse(String code, Map<String, String> errors) {}