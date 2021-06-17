package com.test.checkdeposit;

import org.jooq.lambda.Unchecked;

import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


public class GenerateFileUtility {

    public static void main(String[] args) throws Exception {
        int records = 5;
        Path pathUrl = Paths.get("C:\\Personal\\work\\", "input_"+records+".dat");
        BufferedWriter bufferedWriter = Files.newBufferedWriter(pathUrl);
        List<String> lineList = getFormattedString(records);
        lineList.stream().forEach(l -> {
            Unchecked.consumer((String x) -> bufferedWriter.write(x)).accept(l);
            Unchecked.consumer(x -> bufferedWriter.newLine()).accept(null);
        });
        bufferedWriter.flush();
        bufferedWriter.close();
    }

    public static List<String> getFormattedString(int recordCount) {
        AtomicInteger atomicInteger = new AtomicInteger();
        Supplier<String> numberStrSupplier = () -> padZerosToRight(String.valueOf(atomicInteger.incrementAndGet()), 10);
        List<CheckDeposit> checkDeposits = Stream.generate(() -> new CheckDeposit("CU".concat(numberStrSupplier.get()), "CHK".concat(numberStrSupplier.get()), "A".concat(numberStrSupplier.get()), "A".concat(numberStrSupplier.get()), "Vasanth", "Kannan", "CHASE", "100")).limit(recordCount).collect(Collectors.toList());
        String[] formatArr = {"%-10s", "%-10s", "%-10s", "%-10s", "%-20s", "%-20s", "%-15s", "%-5s"};
        BiFunction<String, Integer, String> biFu = (s, n) -> String.format(formatArr[n], s);
        final StringBuilder strBuilder = new StringBuilder();
        List<String> lineList = checkDeposits.stream().map(c -> strBuilder.append(biFu.apply(c.getCustomerNumber(), 0))
                .append(biFu.apply(c.getCheckNumber(), 1))
                .append(biFu.apply(c.getFromAccountNumber(), 2))
                .append(biFu.apply(c.getToAccountNumber(), 3))
                .append(biFu.apply(c.getFirstName(), 4))
                .append(biFu.apply(c.getLastName(), 5))
                .append(biFu.apply(c.getBankName(), 6))
                .append(biFu.apply(c.getAmount(), 7))
                .toString()
        ).collect(Collectors.mapping(x -> {
            strBuilder.setLength(0);
            return  x;}, Collectors.toList()));

        return lineList;
    }

    private static String padZerosToRight(String value, int padNumber) {
        String paddedValue = IntStream.generate(() -> 0).mapToObj(String::valueOf).limit(padNumber).collect(Collectors.joining());
        int padValLength = paddedValue.length();
        return paddedValue.substring(padValLength - padNumber,padValLength);
    }
}
