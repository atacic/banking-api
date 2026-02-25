package com.aleksa.banking_api.service;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AccountService {
    AccountResponse createAccount(AccountCreateRequest request);
    AccountResponse patchAccount(Long accountId, AccountPatchRequest request);
    AccountResponse getAccountById(Long accountId, User authUser);
    Page<AccountResponse> getAccounts(Pageable pageable);
    void deleteAccount(Long accountId);
}
