package com.nowonbun.selenium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SeleniumAsyncConfig {

  @Value("${app.job-thread.size}")
  private int jobThreadSize;

  @Bean(name = "seleniumExecutor")
  public ThreadPoolTaskExecutor seleniumExecutor() {
    ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
    ex.setCorePoolSize(jobThreadSize); // 最大に2
    ex.setMaxPoolSize(jobThreadSize); // 最大に2
    ex.setQueueCapacity(1000); // FIFO構造
    ex.setWaitForTasksToCompleteOnShutdown(true);
    ex.setAwaitTerminationSeconds(30);
    ex.initialize();
    return ex;
  }
}

