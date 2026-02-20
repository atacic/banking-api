package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.config.RedisConfig;
import com.aleksa.banking_api.dto.request.TransferCreateRequest;
import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.exception.BadRequestException;
import com.aleksa.banking_api.exception.NotFoundException;
import com.aleksa.banking_api.mapper.TransferMapper;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.Transfer;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import com.aleksa.banking_api.model.enums.TransferStatus;
import com.aleksa.banking_api.repoistory.AccountRepository;
import com.aleksa.banking_api.repoistory.TransactionRepository;
import com.aleksa.banking_api.repoistory.TransferRepository;
import com.aleksa.banking_api.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferStatusService transferStatusService;
    private final TransactionStatusService transactionStatusService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper mapper;

    @Transactional
//    @Caching(evict = { // TODO
//            @CacheEvict(
//                    value = RedisConfig.CACHE_NAME_ACCOUNTS,
//                    key = "#request.fromAccountNumber()"
//            ),
//            @CacheEvict(
//                    value = RedisConfig.CACHE_NAME_ACCOUNTS,
//                    key = "#request.toAccountNumber()"
//            )
//    })
    public TransferResponse createTransfer(TransferCreateRequest request) {

        List<Account> accounts = accountRepository.findBothByAccountNumberWithLock(request.fromAccountNumber(), request.toAccountNumber());

        Map<String, Account> accountMap = accounts.stream()
                .collect(Collectors.toMap(Account::getAccountNumber, Function.identity()));

        Account fromAccount = accountMap.get(request.fromAccountNumber());
        Account toAccount = accountMap.get(request.toAccountNumber());

        if (fromAccount == null) {
            throw new NotFoundException("Source account not found");
        } else if (toAccount == null) {
            throw new NotFoundException("Target account not found");
        }

        if (fromAccount.getBalance().compareTo(request.amount()) < 0) {
            throw new BadRequestException("Insufficient funds in source account");
        }

        // Transfer
        Transfer transfer = transferStatusService.createPendingTransfer(request.description(), request.amount(), fromAccount, toAccount);

        // Source transaction
        Transaction transactionOut = transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.TRANSFER_OUT, fromAccount);

        // Target transaction
        Transaction transactionIn = transactionStatusService.createPendingTransaction(request.description(), request.amount(), TransactionType.TRANSFER_IN, toAccount);

        try {
            // FINALIZE transactions
            transactionOut.setBalanceAfter(fromAccount.getBalance().subtract(request.amount()));
            transactionIn.setBalanceAfter(toAccount.getBalance().add(request.amount()));
            transactionOut.setStatus(TransactionStatus.COMPLETED);
            transactionIn.setStatus(TransactionStatus.COMPLETED);
            transactionRepository.save(transactionOut);
            transactionRepository.save(transactionIn);

            // Update accounts balances
            fromAccount.setBalance(fromAccount.getBalance().subtract(request.amount()));
            toAccount.setBalance(toAccount.getBalance().add(request.amount()));
            accountRepository.save(fromAccount);
            accountRepository.save(toAccount);

            // FINALIZE transfer
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer = transferRepository.save(transfer);

            return mapper.transferToTransferResponse(transfer);

        } catch (RuntimeException exception) {
            transactionStatusService.markTransactionFailed(transactionOut.getId(), exception.getMessage());
            transactionStatusService.markTransactionFailed(transactionIn.getId(), exception.getMessage());
            transferStatusService.markTransferFailed(transfer.getId(), exception.getMessage());
            throw exception;
        }
    }
}
