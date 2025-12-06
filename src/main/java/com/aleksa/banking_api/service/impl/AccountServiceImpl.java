package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.mapper.AccountMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper mapper;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {

        Account account = mapper.accountCreateRequestToAccount(request);
        User user = userRepository.findByEmail(request.userEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + request.userEmail() + " not found"));
        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.save(account);

        return mapper.accountToAccountResponse(account);
    }
}
