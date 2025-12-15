package com.aleksa.banking_api.exception;

import com.aleksa.banking_api.exception.response.AccountExistExceptionResponse;
import com.aleksa.banking_api.exception.response.BadRequestExceptionResponse;
import com.aleksa.banking_api.exception.response.NotFoundExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
