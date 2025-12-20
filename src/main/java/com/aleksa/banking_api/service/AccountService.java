package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountCreateRequest request);
    AccountResponse patchAccount(Long accountId, AccountPatchRequest request);

    AccountResponse getAccountById(Long id);

    List<AccountResponse> getAllAccounts();

}
