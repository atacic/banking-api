package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transfer;
import com.aleksa.banking_api.model.enums.TransferStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class TransferMapperTest {

    private final TransferMapper transferMapper = Mappers.getMapper(TransferMapper.class);

    @Test
    void shouldMapTransferToTransferResponse() {

        // Given
        Account fromAccount = new Account();
        fromAccount.setAccountNumber("FROM-123");
        fromAccount.setBalance(BigDecimal.valueOf(500.00));

        Account toAccount = new Account();
        toAccount.setAccountNumber("TO-456");
        toAccount.setBalance(BigDecimal.valueOf(300.00));

        Transfer transfer = new Transfer();
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setAmount(BigDecimal.valueOf(100.00));
        transfer.setStatus(TransferStatus.COMPLETED);

        // When
        TransferResponse response = transferMapper.transferToTransferResponse(transfer);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.fromAccount()).isEqualTo("FROM-123");
        assertThat(response.toAccount()).isEqualTo("TO-456");
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(response.fromBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(500.00));
        assertThat(response.toBalanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(300.00));
        assertThat(response.status()).isEqualTo(TransferStatus.COMPLETED);
    }
}

