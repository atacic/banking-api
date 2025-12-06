package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.repoistory.TransactionRepository;
import com.aleksa.banking_api.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
}
