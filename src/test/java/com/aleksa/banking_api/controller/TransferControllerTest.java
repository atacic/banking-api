package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.model.enums.TransferStatus;
import com.aleksa.banking_api.security.JwtAuthenticationFilter;
import com.aleksa.banking_api.service.TransferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = TransferController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { JwtAuthenticationFilter.class })
        }
)
@AutoConfigureMockMvc(addFilters = false)
class TransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TransferService transferService;

    @Test
    void shouldCreateTransferSuccessfully() throws Exception {

        // Given
        TransferCreateRequest request = new TransferCreateRequest("ACC1", "ACC2", BigDecimal.valueOf(100), "Test");

        TransferResponse responseBody = new TransferResponse(
                "ACC1",
                "ACC2",
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(100),
                BigDecimal.valueOf(200),
                TransferStatus.COMPLETED
        );

        when(transferService.createTransfer(request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/v1/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(transferService).createTransfer(request);
    }

    @Test
    void shouldReturnBadRequestWhenTransferValidationFails() throws Exception {

        // Given: blank from/to account numbers, non-positive amount, blank description
        TransferCreateRequest invalidRequest = new TransferCreateRequest(" ", "", BigDecimal.ZERO, " ");

        // When & Then
        mockMvc.perform(post("/api/v1/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(transferService);
    }
}

