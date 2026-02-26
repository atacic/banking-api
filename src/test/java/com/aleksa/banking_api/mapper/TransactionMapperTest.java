package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.model.Account;
import com.aleksa.banking_api.model.Transaction;
import com.aleksa.banking_api.model.enums.TransactionStatus;
import com.aleksa.banking_api.model.enums.TransactionType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionMapperTest {

    private final TransactionMapper transactionMapper = Mappers.getMapper(TransactionMapper.class);

    @Test
    void shouldMapTransactionToTransactionResponse() {

        // Given
        Account account = new Account();
        account.setId(10L);

        Transaction transaction = new Transaction();
        transaction.setId(1L);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setAmount(BigDecimal.valueOf(100.00));
        transaction.setBalanceAfter(BigDecimal.valueOf(200.00));
        transaction.setDescription("Test transaction");
        transaction.setAccount(account);

        // When
        TransactionResponse response = transactionMapper.transactionToTransactionResponse(transaction);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(response.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(response.amount()).isEqualByComparingTo(BigDecimal.valueOf(100.00));
        assertThat(response.balanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(200.00));
        assertThat(response.description()).isEqualTo("Test transaction");
        assertThat(response.accountId()).isEqualTo(10L);
    }

    @Test
    void shouldMapTransactionsToTransactionResponses() {

        // Given
        Account account1 = new Account();
        account1.setId(1L);

        Account account2 = new Account();
        account2.setId(2L);

        Transaction transaction1 = new Transaction();
        transaction1.setId(100L);
        transaction1.setType(TransactionType.DEPOSIT);
        transaction1.setStatus(TransactionStatus.COMPLETED);
        transaction1.setAmount(BigDecimal.valueOf(50.00));
        transaction1.setBalanceAfter(BigDecimal.valueOf(150.00));
        transaction1.setDescription("First transaction");
        transaction1.setAccount(account1);

        Transaction transaction2 = new Transaction();
        transaction2.setId(200L);
        transaction2.setType(TransactionType.WITHDRAWAL);
        transaction2.setStatus(TransactionStatus.PENDING);
        transaction2.setAmount(BigDecimal.valueOf(25.00));
        transaction2.setBalanceAfter(BigDecimal.valueOf(75.00));
        transaction2.setDescription("Second transaction");
        transaction2.setAccount(account2);

        List<Transaction> transactions = List.of(transaction1, transaction2);

        // When
        List<TransactionResponse> responses = transactionMapper.transactionsToTransactionResponses(transactions);

        // Then
        assertThat(responses).hasSize(2);

        TransactionResponse first = responses.getFirst();
        assertThat(first.id()).isEqualTo(100L);
        assertThat(first.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(first.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(first.amount()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        assertThat(first.balanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
        assertThat(first.description()).isEqualTo("First transaction");
        assertThat(first.accountId()).isEqualTo(1L);

        TransactionResponse second = responses.get(1);
        assertThat(second.id()).isEqualTo(200L);
        assertThat(second.type()).isEqualTo(TransactionType.WITHDRAWAL);
        assertThat(second.status()).isEqualTo(TransactionStatus.PENDING);
        assertThat(second.amount()).isEqualByComparingTo(BigDecimal.valueOf(25.00));
        assertThat(second.balanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(75.00));
        assertThat(second.description()).isEqualTo("Second transaction");
        assertThat(second.accountId()).isEqualTo(2L);
    }
}

