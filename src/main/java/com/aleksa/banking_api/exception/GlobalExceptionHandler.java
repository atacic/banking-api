package com.aleksa.banking_api.exception;

import com.aleksa.banking_api.exception.response.RoleNotFoundExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RoleNotFoundException.class)
    public final ResponseEntity<RoleNotFoundExceptionResponse> handleRoleNotFoundException(RoleNotFoundException exception) {
        RoleNotFoundExceptionResponse response = new RoleNotFoundExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
