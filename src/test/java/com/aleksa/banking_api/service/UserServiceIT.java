package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.LoginRequest;
import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.LoginResponse;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.RoleName;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.RoleRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserServiceIT extends IntegrationTestBase {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = new Role();
        userRole.setRoleName(RoleName.ROLE_USER);
        roleRepository.save(userRole);
    }

    @AfterEach
    void clean() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
    }

    @Test
    void shouldRegisterUserSuccessfully_withDefaultUserRole() {

        // Given
        RegisterRequest request = new RegisterRequest(
                "john@test.com",
                "password",
                "John Doe",
                RoleName.ROLE_USER
        );

        // When
        RegisterResponse response = userService.registerUser(request);

        // Then
        assertThat(response.email()).isEqualTo("john@test.com");
        assertThat(response.fullName()).isEqualTo("John Doe");

        User savedUser = userRepository.findByEmail("john@test.com").orElseThrow();
        assertThat(savedUser.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getRoleName())
                .isEqualTo(RoleName.ROLE_USER);
    }

    @Test
    void shouldRegisterUserSuccessfully_whenRoleIsNotProvided_fallbackToUserRole() {

        // Given
        RegisterRequest request = new RegisterRequest(
                "mark@test.com",
                "password",
                "Mark Smith",
                null
        );

        // When
        RegisterResponse response = userService.registerUser(request);

        // Then
        assertThat(response.email()).isEqualTo("mark@test.com");

        User savedUser = userRepository.findByEmail("mark@test.com").orElseThrow();
        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().iterator().next().getRoleName())
                .isEqualTo(RoleName.ROLE_USER);
    }

    @Test
    void shouldFailRegisterWhenDefaultRoleDoesNotExist() {

        // Given
        roleRepository.deleteAll(); // remove ROLE_USER

        RegisterRequest request = new RegisterRequest(
                "fail@test.com",
                "password",
                "Fail User",
                null
        );

        // When, Then
        assertThatThrownBy(() -> userService.registerUser(request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldLoginUserSuccessfully() {

        // Given
        RegisterRequest registerRequest = new RegisterRequest(
                "login@test.com",
                "password",
                "Login User",
                RoleName.ROLE_USER
        );
        userService.registerUser(registerRequest);

        LoginRequest loginRequest = new LoginRequest(
                "login@test.com",
                "password"
        );

        // When
        LoginResponse response = userService.loginUser(loginRequest);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.token()).isNotBlank();
    }

    @Test
    void shouldFailLoginWhenPasswordIsWrong() {

        // Given
        RegisterRequest registerRequest = new RegisterRequest(
                "wrong@test.com",
                "password",
                "Wrong Password User",
                RoleName.ROLE_USER
        );
        userService.registerUser(registerRequest);

        LoginRequest loginRequest = new LoginRequest(
                "wrong@test.com",
                "bad-password"
        );

        // When, Then
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void shouldFailLoginWhenUserDoesNotExist() {

        // Given
        LoginRequest loginRequest = new LoginRequest(
                "missing@test.com",
                "password"
        );

        // When, Then
        assertThatThrownBy(() -> userService.loginUser(loginRequest))
                .isInstanceOf(RuntimeException.class);
    }
}