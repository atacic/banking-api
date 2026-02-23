package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account", description = "Endpoints for managing user accounts")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/account")
public class AccountController {

    private final AccountService accountService;

    @Operation(
            summary = "Create a new account",
            description = "Creates a new bank account based on the provided details"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account successfully created",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody @Valid AccountCreateRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get account by ID",
            description = "Retrieves account details. Users can only access their own accounts (unless admin)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account found",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - not your account", content = @Content),
            @ApiResponse(responseCode = "404", description = "Account not found", content = @Content)
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccountById(@Parameter(description = "Account ID") @PathVariable @Valid Long accountId,
                                                          @AuthenticationPrincipal User authUser) {
        AccountResponse response = accountService.getAccountById(accountId, authUser);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all accounts", description = "Returns a list of all accounts (typically admin only)")
    @ApiResponse(responseCode = "200", description = "List of accounts",
            content = @Content(schema = @Schema(implementation = AccountResponse.class)))
    @GetMapping
    public ResponseEntity<List<AccountResponse>> getAllAccounts() {
        List<AccountResponse> responses = accountService.getAllAccounts();
        return ResponseEntity.ok(responses);
    }

    @Operation(
            summary = "Partially update an account",
            description = "Allows partial modification of account fields (PATCH)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Account successfully updated"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "400", description = "Invalid data")
    })
    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> patchAccount(@Parameter(description = "Account ID") @PathVariable Long accountId,
                                                        @RequestBody @Valid AccountPatchRequest request) {
        AccountResponse response = accountService.patchAccount(accountId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete an account", description = "Deletes the account and all related transactions")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Account successfully deleted"),
            @ApiResponse(responseCode = "404", description = "Account not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@Parameter(description = "Account ID") @PathVariable Long accountId) {
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
