package com.aleksa.banking_api.controller;

import com.aleksa.banking_api.dto.request.LoginRequest;
import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.LoginResponse;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.model.RoleName;
import com.aleksa.banking_api.security.JwtAuthenticationFilter;
import com.aleksa.banking_api.service.UserService;
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

import java.util.Set;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = { JwtAuthenticationFilter.class })
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void shouldRegisterUserSuccessfully() throws Exception {

        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "Test User", RoleName.ROLE_USER);
        RegisterResponse responseBody = new RegisterResponse("test@example.com", "Test User", Set.of(RoleName.ROLE_USER));

        when(userService.registerUser(request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(userService).registerUser(request);
    }

    @Test
    void shouldReturnBadRequestWhenRegisterValidationFails() throws Exception {

        // Given: invalid email, blank password and fullName
        RegisterRequest invalidRequest = new RegisterRequest("not-an-email", "", " ", RoleName.ROLE_USER);

        // When & Then
        mockMvc.perform(post("/api/v1/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(userService);
    }

    @Test
    void shouldLoginUserSuccessfully() throws Exception {

        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password");
        LoginResponse responseBody = new LoginResponse("jwt-token", true);

        when(userService.loginUser(request)).thenReturn(responseBody);

        // When & Then
        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(userService).loginUser(request);
    }
}

