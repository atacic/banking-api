package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.request.TransactionCreateRequest;
import com.aleksa.banking_api.dto.response.TransactionResponse;
import com.aleksa.banking_api.model.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransactionMapper {

    @Mapping(source = "account.id", target = "accountId")
    TransactionResponse transactionToTransactionResponse(Transaction transaction);

    Transaction transactionCreateRequestToTransaction(TransactionCreateRequest request);
}
