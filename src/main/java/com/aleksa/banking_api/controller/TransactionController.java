package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionCreateRequest request) {
        TransactionResponse response = transactionService.createTransaction(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponse> createTransfer(@RequestBody TransferCreateRequest request) {
        TransferResponse response = transactionService.createTransfer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable Long transactionId) {
       return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(@RequestParam(required = false) Long accountId) {
       return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> patchTransaction(@PathVariable Long transactionId, @RequestBody TransactionPatchRequest request) {
        return ResponseEntity.ok(transactionService.patchTransaction(transactionId, request));
    }
}
