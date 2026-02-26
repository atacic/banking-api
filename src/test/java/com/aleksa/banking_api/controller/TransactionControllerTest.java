package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.request.TransactionPatchRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.security.JwtAuthenticationFilter;
import com.aleksa.banking_api.service.TransactionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TransactionController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { JwtAuthenticationFilter.class })
        }
)
@AutoConfigureMockMvc(addFilters = false)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransactionService transactionService;

    @Test
    void shouldDepositSuccessfully() throws Exception {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(1L, BigDecimal.valueOf(100), "Deposit");
        TransactionResponse responseBody = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                "Deposit",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );

        when(transactionService.deposit(request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/v1/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.type").value(TransactionType.DEPOSIT.name()));

        verify(transactionService).deposit(request);
    }

    @Test
    void shouldReturnBadRequestWhenDepositValidationFails() throws Exception {

        // Given: missing accountId, non-positive amount, blank description
        TransactionCreateRequest invalidRequest = new TransactionCreateRequest(null, BigDecimal.ZERO, " ");

        // When & Then
        mockMvc.perform(post("/api/v1/transaction/deposit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transactionService);
    }

    @Test
    void shouldWithdrawSuccessfully() throws Exception {

        // Given
        TransactionCreateRequest request = new TransactionCreateRequest(1L, BigDecimal.valueOf(50), "Withdrawal");
        TransactionResponse responseBody = new TransactionResponse(
                2L,
                TransactionType.WITHDRAWAL,
                TransactionStatus.COMPLETED,
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(150),
                "Withdrawal",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );

        when(transactionService.withdrawal(request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/v1/transaction/withdrawal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2L))
                .andExpect(jsonPath("$.type").value(TransactionType.WITHDRAWAL.name()));

        verify(transactionService).withdrawal(request);
    }

    @Test
    void shouldGetTransactionByIdSuccessfully() throws Exception {

        // Given
        Long transactionId = 1L;
        TransactionResponse responseBody = new TransactionResponse(
                transactionId,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.TEN,
                "Test",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );

        when(transactionService.getTransactionById(transactionId)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(get("/api/v1/transaction/{transactionId}", transactionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.type").value(TransactionType.DEPOSIT.name()));

        verify(transactionService).getTransactionById(transactionId);
    }

    @Test
    void shouldGetTransactionsByAccountIdSuccessfully() throws Exception {

        // Given
        Long accountId = 1L;
        TransactionResponse tr1 = new TransactionResponse(
                1L,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.valueOf(110),
                "Deposit",
                LocalDateTime.now(),
                LocalDateTime.now(),
                accountId
        );
        TransactionResponse tr2 = new TransactionResponse(
                2L,
                TransactionType.WITHDRAWAL,
                TransactionStatus.COMPLETED,
                BigDecimal.valueOf(5),
                BigDecimal.valueOf(105),
                "Withdrawal",
                LocalDateTime.now(),
                LocalDateTime.now(),
                accountId
        );

        List<TransactionResponse> responses = List.of(tr1, tr2);

        when(transactionService.getTransactionsByAccountId(accountId)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/transaction")
                        .param("accountId", String.valueOf(accountId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        verify(transactionService).getTransactionsByAccountId(accountId);
    }

    @Test
    void shouldCallServiceWithNullAccountIdWhenNotProvided() throws Exception {

        // Given
        Long accountId = null;
        List<TransactionResponse> responses = List.of();

        when(transactionService.getTransactionsByAccountId(accountId)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/transaction"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(transactionService).getTransactionsByAccountId(accountId);
    }

    @Test
    void shouldPatchTransactionSuccessfully() throws Exception {

        // Given
        Long transactionId = 1L;
        TransactionPatchRequest request = new TransactionPatchRequest(TransactionStatus.COMPLETED, "Updated");

        TransactionResponse responseBody = new TransactionResponse(
                transactionId,
                TransactionType.DEPOSIT,
                TransactionStatus.COMPLETED,
                BigDecimal.TEN,
                BigDecimal.TEN,
                "Updated",
                LocalDateTime.now(),
                LocalDateTime.now(),
                1L
        );

        when(transactionService.patchTransaction(transactionId, request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(patch("/api/v1/transaction/{transactionId}", transactionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transactionId))
                .andExpect(jsonPath("$.status").value(TransactionStatus.COMPLETED.name()))
                .andExpect(jsonPath("$.description").value("Updated"));

        verify(transactionService).patchTransaction(transactionId, request);
    }
}
