package com.aleksa.banking_api.mapper;

import com.aleksa.banking_api.dto.response.TransferResponse;
import com.aleksa.banking_api.model.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransferMapper {

    @Mapping(source = "fromAccount.accountNumber", target = "fromAccount")
    @Mapping(source = "toAccount.accountNumber", target = "toAccount")
    @Mapping(source = "fromAccount.balance", target = "fromBalanceAfter")
    @Mapping(source = "toAccount.balance", target = "toBalanceAfter")
    TransferResponse transferToTransferResponse(Transfer transfer);
}
