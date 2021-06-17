package com.test.checkdeposit.service;

import com.test.checkdeposit.CheckDeposit;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface CheckDepositService {

    List<CompletableFuture<CheckDeposit>> callBankAcctInfo(List<CheckDeposit> checkDeposits) throws  Exception;
    List<CompletableFuture<CheckDeposit>> callPartyId(List<CheckDeposit> checkDeposits) throws  Exception;
    List<CheckDeposit> callCloakApi(List<CheckDeposit> checkDeposits) throws  Exception;
    List<CompletableFuture<CheckDeposit>> callEdp(List<CheckDeposit> checkDeposits) throws  Exception;
}
