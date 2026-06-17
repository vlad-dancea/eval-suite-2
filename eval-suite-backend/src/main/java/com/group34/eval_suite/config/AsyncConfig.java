package com.group34.eval_suite.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class AsyncConfig {

  /**
   * Dedicated executor for evaluation-run pipelines. Each run (plus its automatic-improvement loop)
   * occupies one worker thread for its whole lifetime, so the pool is intentionally small with a
   * bounded queue to avoid unbounded LLM fan-out.
   */
  @Bean(name = "runExecutor")
  public Executor runExecutor() {
    final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("run-exec-");
    executor.initialize();
    return executor;
  }
}
