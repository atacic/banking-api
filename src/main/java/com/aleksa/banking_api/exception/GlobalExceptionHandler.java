package com.aleksa.banking_api.exception;

import com.aleksa.banking_api.exception.response.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<BadRequestExceptionResponse> handleBadRequestException(BadRequestException exception) {
        BadRequestExceptionResponse response = new BadRequestExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<NotFoundExceptionResponse> handleNotFoundException(NotFoundException exception) {
        NotFoundExceptionResponse response = new NotFoundExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccountExistException.class)
    public final ResponseEntity<AccountExistExceptionResponse> handleAccountExistException(AccountExistException exception) {
        AccountExistExceptionResponse response = new AccountExistExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ForbiddenException.class)
    public final ResponseEntity<ForbiddenExceptionResponse> handleForbiddenException(ForbiddenException exception) {
        ForbiddenExceptionResponse response = new ForbiddenExceptionResponse(exception.getMessage());
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RequestValidationResponse> handleValidationException(MethodArgumentNotValidException exception) {

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return ResponseEntity
                .badRequest()
                .body(new RequestValidationResponse("VALIDATION_ERROR", errors));
    }
}
