package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AccountMapperTest {

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void shouldMapAccountCreateRequestToAccount() {

        // Given
        AccountCreateRequest request = new AccountCreateRequest("EUR", BigDecimal.valueOf(100), "test@test.com");

        // When
        Account account = accountMapper.accountCreateRequestToAccount(request);

        // Then
        assertThat(account).isNotNull();
        assertThat(account.getCurrency()).isEqualTo("EUR");
        assertThat(account.getBalance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldMapAccountToAccountResponse() {

        // Given
        User user = new User();
        user.setEmail("user@test.com");

        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("ACC123");
        account.setCurrency("USD");
        account.setBalance(BigDecimal.valueOf(500));
        account.setStatus(AccountStatus.ACTIVE);
        account.setUser(user);

        // When
        AccountResponse response = accountMapper.accountToAccountResponse(account);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.accountNumber()).isEqualTo("ACC123");
        assertThat(response.currency()).isEqualTo("USD");
        assertThat(response.balance()).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(response.userEmail()).isEqualTo("user@test.com");
        assertThat(response.status()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void shouldMapAccountsPageToAccountResponsesPage() {

        // Given
        User user1 = new User();
        user1.setEmail("first@test.com");

        User user2 = new User();
        user2.setEmail("second@test.com");

        Account account1 = new Account();
        account1.setId(1L);
        account1.setAccountNumber("ACC-1");
        account1.setCurrency("EUR");
        account1.setBalance(BigDecimal.valueOf(100));
        account1.setStatus(AccountStatus.ACTIVE);
        account1.setUser(user1);

        Account account2 = new Account();
        account2.setId(2L);
        account2.setAccountNumber("ACC-2");
        account2.setCurrency("USD");
        account2.setBalance(BigDecimal.valueOf(200));
        account2.setStatus(AccountStatus.ACTIVE);
        account2.setUser(user2);

        Page<Account> page = new PageImpl<>(List.of(account1, account2));

        // When
        Page<AccountResponse> result = accountMapper.accountsToAccountResponses(page);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);

        AccountResponse first = result.getContent().getFirst();
        assertThat(first.id()).isEqualTo(1L);
        assertThat(first.accountNumber()).isEqualTo("ACC-1");
        assertThat(first.currency()).isEqualTo("EUR");
        assertThat(first.balance()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(first.userEmail()).isEqualTo("first@test.com");

        AccountResponse second = result.getContent().get(1);
        assertThat(second.id()).isEqualTo(2L);
        assertThat(second.accountNumber()).isEqualTo("ACC-2");
        assertThat(second.currency()).isEqualTo("USD");
        assertThat(second.balance()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(second.userEmail()).isEqualTo("second@test.com");
    }
}
