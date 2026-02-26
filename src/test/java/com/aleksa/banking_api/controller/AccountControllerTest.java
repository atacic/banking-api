package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.security.JwtAuthenticationFilter;
import com.aleksa.banking_api.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(
        controllers = AccountController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { JwtAuthenticationFilter.class })
        }
)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AccountService accountService;

    @Test
    void shouldCreateAccountSuccessfully() throws Exception {

        // Given
        AccountCreateRequest request = new AccountCreateRequest("EUR", BigDecimal.valueOf(100), "test@test.com");
        AccountResponse responseBody = new AccountResponse(
                1L,
                "ACC123",
                "EUR",
                BigDecimal.valueOf(100),
                AccountStatus.ACTIVE,
                "test@test.com"
        );

        when(accountService.createAccount(any(AccountCreateRequest.class))).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("ACC123"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.balance").value(100));

        verify(accountService).createAccount(any(AccountCreateRequest.class));
    }

    @Test
    void shouldReturnBadRequestWhenCreateAccountValidationFails() throws Exception {

        // Given: invalid currency, negative balance, invalid email
        AccountCreateRequest invalidRequest = new AccountCreateRequest(
                "INVALID",
                BigDecimal.valueOf(-10),
                "not-an-email"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(accountService);
    }

    @Test
    void shouldGetAccountByIdSuccessfully() throws Exception {

        // Given
        Long accountId = 1L;
        AccountResponse responseBody = new AccountResponse(
                accountId,
                "ACC123",
                "EUR",
                BigDecimal.valueOf(100),
                AccountStatus.ACTIVE,
                "test@test.com"
        );

        when(accountService.getAccountById(eq(accountId), any()))
                .thenReturn(responseBody);

        // When & Then
        mockMvc.perform(get("/api/v1/account/{accountId}", accountId))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.accountNumber").value("ACC123"));

        verify(accountService).getAccountById(eq(accountId), any());
    }

    @Test
    void shouldGetAccountsPageSuccessfully() throws Exception {

        // Given
        Pageable pageable = PageRequest.of(0, 10);

        AccountResponse account1 = new AccountResponse(
                1L,
                "ACC1",
                "EUR",
                BigDecimal.valueOf(100),
                AccountStatus.ACTIVE,
                "first@test.com"
        );
        AccountResponse account2 = new AccountResponse(
                2L,
                "ACC2",
                "USD",
                BigDecimal.valueOf(200),
                AccountStatus.ACTIVE,
                "second@test.com"
        );

        Page<AccountResponse> page = new PageImpl<>(List.of(account1, account2), pageable, 2);

        when(accountService.getAccounts(pageable)).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/account")
                        .param("page", "0")
                        .param("size", "10"))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].accountNumber").value("ACC1"));

        verify(accountService).getAccounts(any(Pageable.class));
    }

    @Test
    void shouldPatchAccountSuccessfully() throws Exception {

        // Given
        Long accountId = 1L;
        AccountPatchRequest request = new AccountPatchRequest("EUR", BigDecimal.ZERO, AccountStatus.ACTIVE);

        AccountResponse responseBody = new AccountResponse(
            accountId,
            "ACC123",
            "EUR",
            BigDecimal.ZERO,
            AccountStatus.ACTIVE,
            "test@test.com"
        );

        when(accountService.patchAccount(accountId, request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(patch("/api/v1/account/{accountId}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.balance").value(0));

        verify(accountService).patchAccount(accountId, request);
    }

    @Test
    void shouldDeleteAccountSuccessfully() throws Exception {

        // Given
        Long accountId = 1L;

        // When & Then
        mockMvc.perform(delete("/api/v1/account/{accountId}", accountId))
                .andExpect(status().isNoContent());

        verify(accountService).deleteAccount(accountId);
    }
}

