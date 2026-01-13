package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.UserSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserSecurityServiceIT extends IntegrationTestBase {

    @Autowired
    private UserSecurityService userSecurityService;

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        user = new User(
                LocalDateTime.now(),
                null,
                "security@test.com",
                "password",
                "Security User",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );

        user = userRepository.save(user);
    }

    @Test
    void shouldLoadUserByUsernameSuccessfully() {

        // Given, When
        UserDetails userDetails = userSecurityService.loadUserByUsername("security@test.com");

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("security@test.com");
        assertThat(userDetails.getPassword()).isEqualTo("password");
    }

    @Test
    void shouldFailLoadUserByUsernameWhenUserDoesNotExist() {

        // Given
        String missingEmail = "missing@test.com";

        // When, Then
        assertThatThrownBy(() -> userSecurityService.loadUserByUsername(missingEmail))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldLoadUserByIdSuccessfully() {

        // Given, When
        User loadedUser = userSecurityService.loadUserById(user.getId());

        // Then
        assertThat(loadedUser.getId()).isEqualTo(user.getId());
        assertThat(loadedUser.getEmail()).isEqualTo("security@test.com");
    }

    @Test
    void shouldFailLoadUserByIdWhenUserDoesNotExist() {

        // Given
        Long missingId = 999L;

        // When, Then
        assertThatThrownBy(() -> userSecurityService.loadUserById(missingId))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
