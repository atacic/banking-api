package com.aleksa.banking_api.service.unit;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.exception.ForbiddenException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.AccountMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountMapper mapper;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void shouldCreateAccountSuccessfully() {

        // Given
        AccountCreateRequest request = new AccountCreateRequest("EUR", BigDecimal.valueOf(100), "test@test.com");
        User user = new User();
        user.setEmail("test@test.com");

        Account account = new Account();
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.valueOf(100));

        when(mapper.accountCreateRequestToAccount(request)).thenReturn(account);
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(accountRepository.findByAccountNumber(anyString())).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.accountToAccountResponse(any(Account.class))).thenReturn(new AccountResponse(1L, "ACC123", "EUR", BigDecimal.valueOf(100), AccountStatus.ACTIVE, "test@test.com"));

        // When
        AccountResponse response = accountService.createAccount(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.currency()).isEqualTo("EUR");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowForbiddenExceptionWhenUserNotOwner() {

        // Given
        Long accountId = 1L;
        User authUser = new User();
        authUser.setEmail("auth@test.com");

        User owner = new User();
        owner.setEmail("owner@test.com");

        Account account = new Account();
        account.setUser(owner);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        // When & Then
        assertThatThrownBy(() -> accountService.getAccountById(accountId, authUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenAccountDoesNotExist() {
        // Given
        Long accountId = 99L;
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> accountService.deleteAccount(accountId))
                .isInstanceOf(NotFoundException.class);
    }
}
