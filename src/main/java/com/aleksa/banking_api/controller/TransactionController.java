package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Transaction", description = "Endpoints for deposits and withdrawals on accounts")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Deposit to account", description = "Adds money to the account (deposit)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Deposit successfully recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or account"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@RequestBody @Valid TransactionCreateRequest request) {
        TransactionResponse response = transactionService.deposit(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Withdraw from account", description = "Removes money from the account (withdrawal)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Withdrawal successfully recorded"),
            @ApiResponse(responseCode = "400", description = "Insufficient funds / invalid amount"),
            @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @PostMapping("/withdrawal")
    public ResponseEntity<TransactionResponse> withdrawal(@RequestBody @Valid TransactionCreateRequest request) {
        TransactionResponse response = transactionService.withdrawal(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get transaction by ID")
    @ApiResponse(responseCode = "200", description = "Transaction found")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@Parameter(description = "Transaction ID") @PathVariable Long transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @Operation(
            summary = "Get transactions for account",
            description = "Returns all transactions for a specific account. If no accountId → return all transactions."
    )
    @ApiResponse(responseCode = "200", description = "List of transactions")
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getTransactions(@Parameter(description = "Account ID") @RequestParam(required = false) Long accountId) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccountId(accountId));
    }

    @Operation(summary = "Partially update a transaction", description = "Partial update of transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction updated"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> patchTransaction(@Parameter(description = "Transaction ID") @PathVariable Long transactionId,
                                                                @RequestBody @Valid TransactionPatchRequest request) {
        return ResponseEntity.ok(transactionService.patchTransaction(transactionId, request));
    }
}
