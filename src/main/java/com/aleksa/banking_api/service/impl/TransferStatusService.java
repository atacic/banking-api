package com.aleksa.banking_api.service.impl;

import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transfer;
import com.aleksa.banking_api.model.enums.TransferStatus;
import com.aleksa.banking_api.repoistory.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferStatusService {

    private final TransferRepository transferRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Transfer createPendingTransfer(String description, BigDecimal amount, Account fromAccount, Account toAccount) {
        Transfer transfer = new Transfer();
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setAmount(amount);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setDescription(description);
        transfer.setReference("TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        return transferRepository.save(transfer);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markTransferFailed(Long transferId, String reason) {
        Transfer transfer = transferRepository.findById(transferId).orElseThrow();
        transfer.setStatus(TransferStatus.FAILED);
        transfer.setDescription("FAILED: " + reason);
        transferRepository.save(transfer);
    }
}
