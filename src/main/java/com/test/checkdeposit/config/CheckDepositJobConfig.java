package com.test.checkdeposit.config;

import com.test.checkdeposit.CheckDeposit;
import com.test.checkdeposit.writer.CheckDepositWriter;
import lombok.extern.log4j.Log4j2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.transform.Range;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;


@Configuration
@Log4j2
public class CheckDepositJobConfig {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;


    @Bean
    public Job job() {
        return jobBuilderFactory.get("job")
                .incrementer(new RunIdIncrementer())
                .start(flatFileStep(null))
                .build();
    }

    @Bean
    Step step() {
        return stepBuilderFactory.get("step")
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        System.out.println("Hello world from Batch!!");
                        return RepeatStatus.FINISHED;
                    }
                }).build();
    }

    @Bean
    public Step flatFileStep(CheckDepositWriter checkDepositWriter) {
        return stepBuilderFactory.get("flatFile")
                .<CheckDeposit, CheckDeposit>chunk(10)
                .reader(flatFileItemReader())
                .writer(checkDepositWriter)
                .build();
    }


    @Bean
    public FlatFileItemReader<CheckDeposit> flatFileItemReader() {
        final Resource resource = new FileSystemResource("C:\\Personal\\work\\input_5.dat");
        final Map<String, Object[]> arrMp = populateRangeArr();
        return new FlatFileItemReaderBuilder<CheckDeposit>()
                .name("flatFileReader")
                .resource(resource)
                .fixedLength()
                .columns((Range[]) arrMp.get("ranges"))
                .names((String[]) arrMp.get("names"))
                .targetType(CheckDeposit.class).build();
    }

    @Bean
    public ItemWriter<CheckDeposit> itemWriter() {
        return (items) -> items.forEach(log::info);
    }


    private Map<String, Object[]> populateRangeArr() {
        Map<String, Object[]> outputMp = new LinkedHashMap<>();
        Map<String, String> rgMp = populateLineMap();
        Function<String[], Range> parseToRngFn = a -> new Range(Integer.valueOf(a[0]), Integer.valueOf(a[1]));
        Range[] ranges = new Range[rgMp.size()];
        String[] keys = new String[rgMp.size()];
        AtomicInteger atmcInt = new AtomicInteger(0);
        rgMp.entrySet().forEach(e -> {
            ranges[atmcInt.get()] = parseToRngFn.apply(e.getValue().split(","));
            keys[atmcInt.get()] = e.getKey();
            atmcInt.incrementAndGet();
        });
        outputMp.put("names", keys);
        outputMp.put("ranges", ranges);
        return outputMp;
    }

    private Map<String, String> populateLineMap() {
        Map<String, String> rangeMp = new LinkedHashMap<>();
        rangeMp.put("customerNumber", "1,12");
        rangeMp.put("checkNumber", "12,25");
        rangeMp.put("fromAccountNumber", "25,36");
        rangeMp.put("toAccountNumber", "36,47");
        rangeMp.put("firstName", "47,67");
        rangeMp.put("lastName", "67,87");
        rangeMp.put("bankName", "87,102");
        rangeMp.put("amount", "102,107");
        return rangeMp;
    }


}
