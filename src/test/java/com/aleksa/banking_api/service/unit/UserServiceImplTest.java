package com.aleksa.banking_api.service.unit;

import com.aleksa.banking_api.dto.request.LoginRequest;
import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.LoginResponse;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.UserMapper;
import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.RoleName;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.RoleRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.security.JwtTokenProvider;
import com.aleksa.banking_api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserMapper mapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void shouldRegisterUserWithRequestedRole() {

        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "Test User", RoleName.ROLE_ADMIN);

        Role role = new Role();
        role.setRoleName(RoleName.ROLE_ADMIN);

        User user = new User();
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setStatus(UserStatus.ACTIVE);
        user.setRoles(Collections.singleton(role));

        RegisterResponse response = new RegisterResponse(request.email(), request.fullName(), Set.of(RoleName.ROLE_ADMIN));

        when(roleRepository.findByRoleName(RoleName.ROLE_ADMIN)).thenReturn(Optional.of(role));
        when(mapper.registerRequestToUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(mapper.userToRegisterResponse(user)).thenReturn(response);

        // When
        RegisterResponse result = userService.registerUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.email()).isEqualTo(request.email());
        assertThat(result.roles()).contains(RoleName.ROLE_ADMIN);

        verify(roleRepository).findByRoleName(RoleName.ROLE_ADMIN);
        verify(userRepository).save(user);
        verify(mapper).userToRegisterResponse(user);
    }

    @Test
    void shouldRegisterUserWithDefaultRoleWhenRequestedRoleMissing() {

        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "Test User", RoleName.ROLE_ADMIN);

        Role defaultRole = new Role();
        defaultRole.setRoleName(RoleName.ROLE_USER);

        User user = new User();

        RegisterResponse response = new RegisterResponse(request.email(), request.fullName(), Set.of(RoleName.ROLE_USER));

        when(roleRepository.findByRoleName(RoleName.ROLE_ADMIN)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.of(defaultRole));
        when(mapper.registerRequestToUser(request)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(mapper.userToRegisterResponse(user)).thenReturn(response);

        // When
        RegisterResponse result = userService.registerUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.roles()).contains(RoleName.ROLE_USER);

        verify(roleRepository).findByRoleName(RoleName.ROLE_ADMIN);
        verify(roleRepository).findByRoleName(RoleName.ROLE_USER);
    }

    @Test
    void shouldThrowNotFoundWhenNoRoleConfigured() {

        // Given
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "Test User", RoleName.ROLE_ADMIN);

        when(roleRepository.findByRoleName(RoleName.ROLE_ADMIN)).thenReturn(Optional.empty());
        when(roleRepository.findByRoleName(RoleName.ROLE_USER)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldLoginUserSuccessfully() {

        // Given
        LoginRequest request = new LoginRequest("test@example.com", "password");

        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtTokenProvider.generateToken(authentication)).thenReturn("jwt-token");

        // When
        LoginResponse response = userService.loginUser(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.success()).isTrue();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateToken(authentication);
    }
}

