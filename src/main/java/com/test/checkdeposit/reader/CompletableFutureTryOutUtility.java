package com.test.checkdeposit.reader;

import com.test.checkdeposit.CheckDeposit;
import org.jooq.lambda.Unchecked;
import org.jooq.lambda.fi.util.function.CheckedBiFunction;
import org.jooq.lambda.fi.util.function.CheckedUnaryOperator;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class CompletableFutureTryOutUtility {

    private static ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(26);
    private static CheckedBiFunction<Long, CheckDeposit, CheckDeposit> checkedBiFunction = (l, c) -> sleepForme(l, c);
    private static CheckedUnaryOperator<List<CheckDeposit>> cloakApiFunction = x -> callCloakApi(x);
    private static CheckedUnaryOperator<List<CheckDeposit>> partyIdFunction = x -> callPartyId(x);
    private static CheckedUnaryOperator<List<CheckDeposit>>  edpFunction = x -> callEdp(x);
    private static BiFunction<CheckDeposit,Throwable,CheckDeposit> errorHandlerFunction = (c,t) -> Optional.ofNullable(c).map(ch -> {
        ch.setBankName("Hello");
        return ch;
    }).orElseThrow(() -> new RuntimeException(t));


    public static void main(String[] args) throws Exception {
        System.out.println("Job Started!");
        long start = System.nanoTime();
        //prepareInput
        //bankAcctInfo (ChkDpst) -> setCifAndAcctNumber 10ms
        // partyService -> takes CIf and outs partyId 30ms
        //cloakApi -> cloak AcctNumber for entire list 20ms
        //EDP -> (cloakedAcctNumbers, cif, partyId) -> set the ScheduleId 20025ms
//        syncCall();
        try{
            asynCall();
        }
        catch (Exception ex){
            System.out.println("Caught Exception"+ex);
        }

//        System.out.println("Job Ended in NS:" + (System.nanoTime() - start));
//        System.out.println("Job Ended in MS:" + TimeUnit.NANOSECONDS.toMillis((System.nanoTime() - start)));

    }

    public static void syncCall() throws Exception {
        List<CheckDeposit> checkDeposits = prepareInput(100);
        callBankAcctInfo(checkDeposits);
        callPartyId(checkDeposits);
        callCloakApi(checkDeposits);
        callEdp(checkDeposits);
    }

    public static void asynCall() throws Exception {
        System.out.println("Async call main Thread"+Thread.currentThread());
        long start = System.nanoTime();
        List<CheckDeposit> checkDeposits = prepareInput(2);
        List<CompletableFuture<CheckDeposit>> bicfs = callBankAcctInfoCf(checkDeposits);
        List<CompletableFuture<CheckDeposit>> pscfs = callpartyIdCf(checkDeposits);
        List<CompletableFuture<CheckDeposit>> edpcfs = callEdpCf(checkDeposits);
        Function<List<CompletableFuture<CheckDeposit>>,CompletableFuture<Void>> allOfFn = x -> CompletableFuture.allOf(x.toArray(new CompletableFuture[x.size()]));
        Consumer<List<CompletableFuture<CheckDeposit>>> printCfStatus = x -> x.forEach(System.out::println);

        CompletableFuture.runAsync(() -> System.out.println("Starting the completable Chain"))
                .thenRun(() -> allOfFn.apply(bicfs).join())
                .thenRun(() -> printCfStatus.accept(bicfs))
               .thenRunAsync(() -> Unchecked.unaryOperator(cloakApiFunction).apply(checkDeposits),threadPoolExecutor)
               .thenRun(() -> allOfFn.apply(pscfs).join())
                .thenRun(() -> printCfStatus.accept(pscfs))
               .thenRun(() -> allOfFn.apply(edpcfs).join())
               .thenRun(() -> {System.out.println("Done from Then Run:"+(System.nanoTime() - start));threadPoolExecutor.shutdown();})
                .exceptionally(x -> {System.out.println("Error occurred!");threadPoolExecutor.shutdown();return  null;});


    }

    public static List<CheckDeposit> prepareInput(int recordCount) {
        AtomicInteger atomicInteger = new AtomicInteger();
        Supplier<String> numberStrSupplier = () -> padZerosToRight(String.valueOf(atomicInteger.incrementAndGet()), 10);
        List<CheckDeposit> checkDeposits = Stream.generate(() -> new CheckDeposit("CU".concat(numberStrSupplier.get()), "CHK".concat(numberStrSupplier.get()), "A".concat(numberStrSupplier.get()), "A".concat(numberStrSupplier.get()), "Vasanth", "Kannan", "CHASE", "100")).limit(recordCount).collect(Collectors.toList());
        return checkDeposits;
    }

    public static List<CheckDeposit> callBankAcctInfo(List<CheckDeposit> checkDeposits) throws Exception {

        for (CheckDeposit checkDeposit : checkDeposits) {
            checkDeposit.setAmount(checkDeposit.getAmount());
            sleepForme(10, checkDeposit);
        }
        return checkDeposits;
    }

    public static List<CompletableFuture<CheckDeposit>> callBankAcctInfoCf(List<CheckDeposit> checkDeposits) throws Exception {
        List<CompletableFuture<CheckDeposit>> checkDepositsCfs = checkDeposits.stream()
                .map(x -> {
                    CompletableFuture<CheckDeposit> cf = CompletableFuture.supplyAsync(() -> Unchecked.biFunction(checkedBiFunction).apply(20l, x),threadPoolExecutor).handle(errorHandlerFunction);
//                    System.out.println("<callBankAcctInfoCf> After Timeout method call!"+Thread.currentThread() +"CF:"+cf);
                    return  cf;
                })
                .collect(Collectors.toList());

        return checkDepositsCfs;
    }

    private static String doSomeCalc() {

        return IntStream.range(0, 25).mapToObj(String::valueOf).collect(Collectors.joining());
    }

    public static List<CheckDeposit> callPartyId(List<CheckDeposit> checkDeposits) throws Exception {

        for (CheckDeposit checkDeposit : checkDeposits) {
            checkDeposit.setBankName(checkDeposit.getBankName());
            sleepForme(30, checkDeposit);
        }

        return checkDeposits;
    }

    public static List<CompletableFuture<CheckDeposit>> callpartyIdCf(List<CheckDeposit> checkDeposits) throws Exception {
        List<CompletableFuture<CheckDeposit>> checkDepositsCfs = checkDeposits.stream()
                .map(x -> {
                    CompletableFuture<CheckDeposit> cf = CompletableFuture.supplyAsync(() -> Unchecked.biFunction(checkedBiFunction).apply(30l, x),threadPoolExecutor).handle(errorHandlerFunction);
//                    System.out.println("<callBankAcctInfoCf> After Timeout method call!"+Thread.currentThread() +"CF:"+cf);
                    return  cf;
                })
                .collect(Collectors.toList());

        return checkDepositsCfs;
    }

    public static List<CheckDeposit> callCloakApi(List<CheckDeposit> checkDeposits) throws Exception {
        for (CheckDeposit checkDeposit : checkDeposits) {
            checkDeposit.setBankName(checkDeposit.getBankName());
        }
        sleepForme(25, null);
        return checkDeposits;
    }



    public static List<CheckDeposit> callEdp(List<CheckDeposit> checkDeposits) throws Exception {
        for (CheckDeposit checkDeposit : checkDeposits) {
            checkDeposit.setBankName(checkDeposit.getBankName());
            sleepForme(2000, checkDeposit);
        }
        return checkDeposits;
    }

    public static List<CompletableFuture<CheckDeposit>> callEdpCf(List<CheckDeposit> checkDeposits) throws Exception {
        List<CompletableFuture<CheckDeposit>> checkDepositsCfs = checkDeposits.stream()
                .map(x -> {
                    CompletableFuture<CheckDeposit> cf = CompletableFuture.supplyAsync(() -> Unchecked.biFunction(checkedBiFunction).apply(2000l, x),threadPoolExecutor).handle(errorHandlerFunction);
//                    System.out.println("<callBankAcctInfoCf> After Timeout method call!"+Thread.currentThread() +"CF:"+cf);
                    return  cf;
                })
                .collect(Collectors.toList());
        return checkDepositsCfs;
    }

    private static String padZerosToRight(String value, int padNumber) {
        String paddedValue = IntStream.generate(() -> 0).mapToObj(String::valueOf).limit(padNumber).collect(Collectors.joining());
        int padValLength = paddedValue.length();
        return paddedValue.substring(padValLength - padNumber, padValLength);
    }

    private static CheckDeposit sleepForme(long timeinMs, CheckDeposit checkDeposit) throws InterruptedException {
        Thread.sleep(timeinMs);
        return checkDeposit;
    }

    public static ThreadPoolExecutor threadPoolExecutor(){
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(2);
        return  threadPoolExecutor;
    }

}
