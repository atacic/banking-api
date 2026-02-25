package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.model.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, imports = Page.class)
public interface AccountMapper {

    Account accountCreateRequestToAccount(AccountCreateRequest request);

    @Mapping(source = "user.email", target = "userEmail")
    AccountResponse accountToAccountResponse(Account account);

    default Page<AccountResponse> accountsToAccountResponses(Page<Account> accounts) {
        return accounts.map(this::accountToAccountResponse);
    }
}
