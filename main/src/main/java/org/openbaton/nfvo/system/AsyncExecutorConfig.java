package org.openbaton.nfvo.system;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** Created by mob on 09/02/2017. */
/*
This class enable the entire application to use methods annotated with @Async
The Executor, which will be used, can be configured here.
 */
@Configuration
@EnableAsync
public class AsyncExecutorConfig extends AsyncConfigurerSupport {

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(10);
    executor.setThreadNamePrefix("OpenBatonAsyncTask-");
    executor.initialize();
    return executor;
  }
}
