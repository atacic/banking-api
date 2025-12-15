package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long accountId) {

//        AccountResponse response = accountService.getAccountById(accountId); // TODO: DOMACI
//        return ResponseEntity.ok(response);
        return null; // TODO: Remove when finish implementation
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {

//        List<AccountResponse> responses = accountService.getAllAccounts(); // TODO: DOMACI
//        return ResponseEntity.ok(responses);
        return null; // TODO: Remove when finish implementation
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> patchAccount(@PathVariable Long accountId, @RequestBody AccountPatchRequest request) {
        AccountResponse response = accountService.patchAccount(accountId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
//        accountService.deleteAccount(accountId); TODO: DOMACI
        return ResponseEntity.noContent().build();
    }
}
