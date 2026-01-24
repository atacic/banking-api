package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.LoginRequest;
import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.LoginResponse;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        RegisterResponse response = userService.registerUser(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        LoginResponse response = userService.loginUser(loginRequest);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }
}
