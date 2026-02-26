package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.request.RegisterRequest;
import com.aleksa.banking_api.dto.response.RegisterResponse;
import com.aleksa.banking_api.model.Role;
import com.aleksa.banking_api.model.RoleName;
import com.aleksa.banking_api.model.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapUserToRegisterResponse() {

        // Given
        Role userRole = new Role();
        userRole.setRoleName(RoleName.ROLE_USER);

        Role adminRole = new Role();
        adminRole.setRoleName(RoleName.ROLE_ADMIN);

        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setRoles(Set.of(userRole, adminRole));

        // When
        RegisterResponse response = userMapper.userToRegisterResponse(user);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.email()).isEqualTo("test@example.com");
        assertThat(response.fullName()).isEqualTo("Test User");
        assertThat(response.roles()).containsExactlyInAnyOrder(RoleName.ROLE_USER, RoleName.ROLE_ADMIN);
    }

    @Test
    void shouldMapRegisterRequestToUserWithEncodedPassword() {

        // Given
        String rawPassword = "plainPassword";
        RegisterRequest request = new RegisterRequest(
                "register@example.com",
                rawPassword,
                "Register User",
                RoleName.ROLE_USER
        );

        // When
        User user = userMapper.registerRequestToUser(request);

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("register@example.com");
        assertThat(user.getFullName()).isEqualTo("Register User");

        String encodedPassword = user.getPassword();
        assertThat(encodedPassword).isNotBlank();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }
}

