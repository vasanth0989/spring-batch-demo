package com.test.checkdeposit.service;

import com.test.checkdeposit.CheckDeposit;
import com.test.checkdeposit.util.CheckDepositConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class CheckDepositServiceImpl implements CheckDepositService {

    private final ThreadPoolTaskExecutor batchJobThreadPoolExecutor;

    private CheckedBiFunction<Long, CheckDeposit, CheckDeposit> checkedBiFunction = (l, c) -> sleepForme(l, c);
    private BiFunction<CheckDeposit, Throwable, CheckDeposit> errorHandlerFunction = (c, t) -> Optional.ofNullable(c).map(ch -> {
        ch.setBankName("Hello");
        return ch;
    }).orElseThrow(() -> new RuntimeException(t));

    @Override
    @Async
    public List<CompletableFuture<CheckDeposit>> callBankAcctInfo(List<CheckDeposit> checkDeposits) throws Exception {
        log.info("callBankAcctInfo {}",Thread.currentThread());
        return checkDeposits.stream()
                .map(x -> CompletableFuture.supplyAsync(() -> Unchecked.biFunction(checkedBiFunction).apply(CheckDepositConstants.BAI_WAIT_TIME, x),batchJobThreadPoolExecutor).handle(errorHandlerFunction))
                .collect(Collectors.toList());
    }

    @Override
    @Async
    public List<CompletableFuture<CheckDeposit>> callPartyId(List<CheckDeposit> checkDeposits) throws Exception {
        log.info("callPartyId {}",Thread.currentThread());
        return checkDeposits.stream()
                .map(x -> CompletableFuture.supplyAsync(() -> Unchecked.biFunction(checkedBiFunction).apply(CheckDepositConstants.PS_WAIT_TIME, x),batchJobThreadPoolExecutor).handle(errorHandlerFunction))
                .collect(Collectors.toList());
    }

    @Override
    public List<CheckDeposit> callCloakApi(List<CheckDeposit> checkDeposits) throws Exception {
        log.info("callCloakApi {}",Thread.currentThread());
        for (CheckDeposit checkDeposit : checkDeposits) {
            checkDeposit.setBankName(checkDeposit.getBankName());
        }
        sleepForme(CheckDepositConstants.CLK_WAIT_TIME, null);
        return checkDeposits;
    }

    @Override
    @Async
    public List<CompletableFuture<CheckDeposit>> callEdp(List<CheckDeposit> checkDeposits) throws Exception {
        return checkDeposits.stream()
                .map(x -> CompletableFuture.supplyAsync(() -> Unchecked.biFunction(checkedBiFunction).apply(CheckDepositConstants.EDP_WAIT_TIME, x),batchJobThreadPoolExecutor).handle(errorHandlerFunction))
                .collect(Collectors.toList());
    }

    private CheckDeposit sleepForme(long timeinMs, CheckDeposit checkDeposit) throws InterruptedException {
        log.info("Inside sleepForme {}",Thread.currentThread());
        Thread.sleep(timeinMs);
        return checkDeposit;
    }
}
