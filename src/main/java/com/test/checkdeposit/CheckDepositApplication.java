package com.test.checkdeposit;

import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@EnableBatchProcessing
@SpringBootApplication
@Log4j2
public class CheckDepositApplication implements  CommandLineRunner{

	@Autowired
	private JobLauncher jobLauncher;
	@Autowired
	private Job job;
	@Autowired
	private JobExplorer jobExplorer;

	public static void main(String[] args) {
		SpringApplication.run(CheckDepositApplication.class, args);
	}


	@Override
	public void run(String... args) throws Exception {
		JobParameters jobParameters = new JobParametersBuilder(jobExplorer).getNextJobParameters(job).toJobParameters();
		jobLauncher.run(job,jobParameters);
	}
}
