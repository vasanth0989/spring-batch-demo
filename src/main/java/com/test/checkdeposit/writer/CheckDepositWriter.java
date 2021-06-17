package com.test.checkdeposit.writer;

import com.test.checkdeposit.CheckDeposit;
import com.test.checkdeposit.service.CheckDepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedUnaryOperator;
import org.springframework.batch.item.ItemWriter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;


@Component
@Log4j2
@RequiredArgsConstructor
public class CheckDepositWriter implements ItemWriter<CheckDeposit> {

   private final  CheckDepositService checkDepositService;
   private final ThreadPoolTaskExecutor batchJobThreadPoolExecutor;

    @Override
    public void write(List<? extends CheckDeposit> items) throws Exception {
        List<CheckDeposit> checkDeposits = (List<CheckDeposit>) items;
        log.info("CheckService {}",checkDeposits);
        List<CompletableFuture<CheckDeposit>> bicfs = checkDepositService.callBankAcctInfo(checkDeposits);
        List<CompletableFuture<CheckDeposit>> pscfs = checkDepositService.callPartyId(checkDeposits);
        List<CompletableFuture<CheckDeposit>> edpcfs = checkDepositService.callEdp(checkDeposits);
        Function<List<CompletableFuture<CheckDeposit>>,CompletableFuture<Void>> allOfFn = x -> CompletableFuture.allOf(x.toArray(new CompletableFuture[x.size()]));
        Consumer<List<CompletableFuture<CheckDeposit>>> printCfStatus = x -> x.forEach(log::info);
        final CheckedUnaryOperator<List<CheckDeposit>> cloakApiFunction = x -> checkDepositService.callCloakApi(x);

        CompletableFuture.runAsync(() -> log.info("Starting the completable Chain"))
                .thenRun(() -> allOfFn.apply(bicfs).join())
                .thenRun(() -> printCfStatus.accept(bicfs))
                .thenRun(() -> Unchecked.unaryOperator(cloakApiFunction).apply(checkDeposits))
                .thenRun(() -> allOfFn.apply(pscfs).join())
                .thenRun(() -> printCfStatus.accept(pscfs))
                .thenRun(() -> allOfFn.apply(edpcfs).join())
                .thenRun(() -> {
                    log.info("Chain completed successfully!");
                    checkDeposits.forEach(log::info);
                    batchJobThreadPoolExecutor.shutdown();
                })
                .exceptionally(x -> {
                    log.info("Async Exception occurred {}",x);
                    batchJobThreadPoolExecutor.shutdown();
                    failJob(x);
                    return  null;
                });

        log.info("Hello form write method after runAsync {}",Thread.currentThread());
    }

    public void failJob(Throwable x) throws RuntimeException
    {
        throw new RuntimeException(x);
    }
}
