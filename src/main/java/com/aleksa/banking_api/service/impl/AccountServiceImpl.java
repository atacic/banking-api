package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.config.RedisConfig;
import com.aleksa.banking_api.dto.request.AccountCreateRequest;
import com.aleksa.banking_api.dto.request.AccountPatchRequest;
import com.aleksa.banking_api.dto.response.AccountResponse;
import com.aleksa.banking_api.exception.AccountExistException;
import com.aleksa.banking_api.exception.ForbiddenException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.AccountMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.User;
import com.aleksa.banking_api.model.enums.AccountStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.UserRepository;
import com.aleksa.banking_api.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        accountRepository.findByAccountNumber(request.accountNumber()).ifPresent((presentedAccount) -> {
            throw new AccountExistException("Account with number: " + presentedAccount.getAccountNumber() + " already exists");
        });

        account.setUser(user);
        account.setStatus(AccountStatus.ACTIVE);

        account = accountRepository.save(account);
        return mapper.accountToAccountResponse(account);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#accountId")
    public AccountResponse patchAccount(Long accountId, AccountPatchRequest request) {

        Account account = accountRepository.findById(accountId).orElseThrow(() -> new NotFoundException("Account with id: " + accountId + " not found"));

        if (request.accountNumber() != null) {
            accountRepository.findByAccountNumber(request.accountNumber()).ifPresent((presentedAccount) -> {
                throw new AccountExistException("Account with number: " + presentedAccount.getAccountNumber() + " already exists");
            });
            account.setAccountNumber(request.accountNumber());
        }

        if (request.currency() != null) {
            account.setCurrency(request.currency());
        }

        if (request.balance() != null) {
            account.setBalance(request.balance());
        }

        if (request.status() != null) {
            account.setStatus(request.status());
        }

        account = accountRepository.save(account);
        return mapper.accountToAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#accountId")
    public AccountResponse getAccountById(Long accountId, User authUser) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + " not found"));

        // Ownership check
        if (!account.getUser().getUsername().equals(authUser.getUsername())) {
            throw new ForbiddenException("Access denied");
        }

        // MapStruct automatski kreira AccountResponse
        return mapper.accountToAccountResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return mapper.accountsToAccountResponses(accounts);
    }

    @Override
    @Transactional
    @CacheEvict(value = RedisConfig.CACHE_NAME_ACCOUNTS, key = "#accountId")
    public void deleteAccount(Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account with id: " + accountId + " not found"));
        accountRepository.deleteById(accountId);
    }
}