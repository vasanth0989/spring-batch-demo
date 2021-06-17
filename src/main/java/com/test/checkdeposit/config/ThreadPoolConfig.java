package com.test.checkdeposit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadPoolConfig {

  @Bean
  public ThreadPoolTaskExecutor batchJobThreadPoolExecutor(){
      ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
      int threadCount = calculateThreads(0.8);
      taskExecutor.setCorePoolSize(threadCount);
      taskExecutor.setMaxPoolSize(threadCount*2);
      taskExecutor.setQueueCapacity(100);
      taskExecutor.initialize();
      return  taskExecutor;
  }

  private int calculateThreads(double blockingCoefficient)
  {
      return (int) Math.round(Runtime.getRuntime().availableProcessors() / (1 - blockingCoefficient));
  }

}
