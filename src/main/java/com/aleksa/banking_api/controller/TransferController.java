package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Transfer", description = "Endpoints for inter-account transfers")
@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/transfer")
public class TransferController {

    private final TransferService transferService;

    @Operation(
            summary = "Create a transfer",
            description = "Transfers money from one account to another"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transfer successfully created and processed",
                    content = @Content(schema = @Schema(implementation = TransferResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid data / insufficient funds"),
            @ApiResponse(responseCode = "404", description = "Source or target account not found")
    })
    @PostMapping
    public ResponseEntity<TransferResponse> createTransfer(@RequestBody @Valid TransferCreateRequest request) {
        TransferResponse response = transferService.createTransfer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get transfer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer found"),
            @ApiResponse(responseCode = "404", description = "Transfer not found")
    })
    @GetMapping("/{transferId}")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable Long transferId) {
//        return ResponseEntity.ok(transferService.getTransferById(transferId)); TODO
        return null;
    }
}
