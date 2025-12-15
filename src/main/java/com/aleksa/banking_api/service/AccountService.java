package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;

public interface AccountService {
    AccountResponse createAccount(AccountCreateRequest request);
    AccountResponse patchAccount(Long accountId, AccountPatchRequest request);
}
