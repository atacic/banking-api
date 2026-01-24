package com.aleksa.banking_api.service;

import com.aleksa.banking_api.IntegrationTestBase;
import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.exception.AccountExistException;
import com.aleksa.banking_api.exception.ForbiddenException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.model.enums.UserStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceIT extends IntegrationTestBase {

    @Autowired
    private AccountServiceImpl accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String USER_EMAIL = "test@test.com";

    private User user;

    @BeforeEach
    void setup() {
        accountRepository.deleteAll();
        userRepository.deleteAll();
        user = new User(
                LocalDateTime.now(),
                null,
                USER_EMAIL,
                "pass",
                "Test User",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );
        user = userRepository.save(user);
    }

    @Test
    void shouldCreateAccountSuccessfully() {

        // Given
        AccountCreateRequest request = new AccountCreateRequest("ACC-111", "EUR", BigDecimal.valueOf(500), USER_EMAIL);

        // When
        AccountResponse response = accountService.createAccount(request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accountNumber()).isEqualTo("ACC-111");
        assertThat(response.currency()).isEqualTo("EUR");
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(500));
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(accountRepository.findByAccountNumber("ACC-111")).isPresent();
    }

    @Test
    void shouldFailCreateWhenUserDoesNotExist() {

        // Given
        AccountCreateRequest request = new AccountCreateRequest("ACC-222", "EUR", BigDecimal.valueOf(100), "missing@mail.com");

        // When, Then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void shouldFailCreateWhenAccountNumberAlreadyExists() {

        // Given
        Account existing = new Account();
        existing.setAccountNumber("ACC-333");
        existing.setCurrency("EUR");
        existing.setBalance(BigDecimal.TEN);
        existing.setStatus(AccountStatus.ACTIVE);
        existing.setUser(user);
        accountRepository.save(existing);

        AccountCreateRequest request = new AccountCreateRequest("ACC-333", "EUR", BigDecimal.valueOf(200), USER_EMAIL);

        // When, Then
        assertThatThrownBy(() -> accountService.createAccount(request))
                .isInstanceOf(AccountExistException.class);
    }

    @Test
    void shouldPatchAccountSuccessfully() {

        // Given
        Account account = new Account();
        account.setAccountNumber("ACC-444");
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.valueOf(50));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);
        account = accountRepository.save(account);

        AccountPatchRequest request = new AccountPatchRequest("ACC-999", "USD", BigDecimal.valueOf(999), AccountStatus.INACTIVE);

        // When
        AccountResponse response = accountService.patchAccount(account.getId(), request);

        // Then
        assertThat(response.accountNumber()).isEqualTo("ACC-999");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.balance()).isEqualTo(BigDecimal.valueOf(999));
        assertThat(response.status()).isEqualTo(AccountStatus.INACTIVE);
    }

    @Test
    void shouldFailPatchWhenAccountNotFound() {

        // Given
        Long accountId = 999L;
        AccountPatchRequest request = new AccountPatchRequest("ANY", "EUR", BigDecimal.TEN, AccountStatus.ACTIVE);

        // When, Then
        assertThatThrownBy(() -> accountService.patchAccount(accountId, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFailPatchWhenNewAccountNumberExists() {

        // Given
        Account acc1 = new Account();
        acc1.setAccountNumber("ACC-1");
        acc1.setCurrency("EUR");
        acc1.setBalance(BigDecimal.ONE);
        acc1.setStatus(AccountStatus.ACTIVE);
        acc1.setUser(user);
        accountRepository.save(acc1);
        Account acc2 = new Account();
        acc2.setAccountNumber("ACC-2");
        acc2.setCurrency("EUR");
        acc2.setBalance(BigDecimal.TEN);
        acc2.setStatus(AccountStatus.ACTIVE);
        acc2.setUser(user);
        acc2 = accountRepository.save(acc2);

        AccountPatchRequest patch = new AccountPatchRequest("ACC-1", null, null, null);

        Long id = acc2.getId();

        // When, Then
        assertThatThrownBy(() -> accountService.patchAccount(id, patch))
                .isInstanceOf(AccountExistException.class);
    }

    @Test
    void shouldGetAccountById() {

        // Given
        Account account = new Account();
        account.setAccountNumber("ACC-777");
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);
        account = accountRepository.save(account);

        // When
        AccountResponse response = accountService.getAccountById(account.getId(), user);

        // Then
        assertThat(response.accountNumber()).isEqualTo("ACC-777");
        assertThat(response.currency()).isEqualTo("EUR");
    }

    @Test
    void shouldFailGetWhenAccountNotFoundById() {

        // Given
        Long accountId = 12345L;

        // When, Then
        assertThatThrownBy(() -> accountService.getAccountById(accountId, user))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldFailGetWhenAccountUserNotEqualToAuthUser() {

        // Given
        Account account = new Account();
        account.setAccountNumber("ACC-777");
        account.setCurrency("EUR");
        account.setBalance(BigDecimal.valueOf(1000));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);
        account = accountRepository.save(account);
        Long accountId = account.getId();

        User differentUser = new User(
                LocalDateTime.now(),
                null,
                "user@email.com",
                "pass",
                "User",
                LocalDateTime.now(),
                UserStatus.ACTIVE
        );

        // When, Then
        assertThatThrownBy(() -> accountService.getAccountById(accountId, differentUser))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void shouldReturnAllAccounts() {

        // Given
        Account a1 = new Account();
        a1.setAccountNumber("X1");
        a1.setCurrency("EUR");
        a1.setBalance(BigDecimal.ONE);
        a1.setStatus(AccountStatus.ACTIVE);
        a1.setUser(user);
        Account a2 = new Account();
        a2.setAccountNumber("X2");
        a2.setCurrency("EUR");
        a2.setBalance(BigDecimal.TEN);
        a2.setStatus(AccountStatus.ACTIVE);
        a2.setUser(user);
        accountRepository.saveAll(List.of(a1, a2));

        // When
        List<AccountResponse> accounts = accountService.getAllAccounts();

        // Then
        assertThat(accounts).hasSize(2);
    }

    @Test
    void shouldDeleteAccountById() {

        // Given
        Account a1 = new Account();
        a1.setAccountNumber("X1");
        a1.setCurrency("EUR");
        a1.setBalance(BigDecimal.ONE);
        a1.setStatus(AccountStatus.ACTIVE);
        a1.setUser(user);
        Account a2 = new Account();
        a2.setAccountNumber("X2");
        a2.setCurrency("EUR");
        a2.setBalance(BigDecimal.TEN);
        a2.setStatus(AccountStatus.ACTIVE);
        a2.setUser(user);
        accountRepository.saveAll(List.of(a1, a2));

        // When
        accountService.deleteAccount(a1.getId());

        // Then
        List<AccountResponse> accounts = accountService.getAllAccounts();
        assertThat(accounts).hasSize(1);
        assertThat(accounts.getFirst().id()).isEqualTo(a2.getId());
    }

    @Test
    void shouldFailDeleteWhenAccountNotExist() {

        // Given
        Long notExistsAccountId = 987312L;
        Account a1 = new Account();
        a1.setAccountNumber("X1");
        a1.setCurrency("EUR");
        a1.setBalance(BigDecimal.ONE);
        a1.setStatus(AccountStatus.ACTIVE);
        a1.setUser(user);
        Account a2 = new Account();
        a2.setAccountNumber("X2");
        a2.setCurrency("EUR");
        a2.setBalance(BigDecimal.TEN);
        a2.setStatus(AccountStatus.ACTIVE);
        a2.setUser(user);
        accountRepository.saveAll(List.of(a1, a2));

        // When, Then
        assertThatThrownBy(() -> accountService.deleteAccount(notExistsAccountId))
                .isInstanceOf(NotFoundException.class);
    }
}
