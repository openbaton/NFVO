package org.openbaton.nfvo.system;

import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
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

  @Value("${nfvo.vmanager.executor.maxpoolsize:-1}")
  private int maxPoolSize;

  @Value("${nfvo.vmanager.executor.corepoolsize:1}")
  private int corePoolSize;

  @Value("${nfvo.vmanager.executor.queuecapacity:0}")
  private int queueCapacity;

  @Value("${nfvo.vmanager.executor.keepalive:60}")
  private int keepAliveSeconds;

  @Override
  @Bean
  public Executor getAsyncExecutor() {
    if (maxPoolSize < 0) maxPoolSize = Integer.MAX_VALUE;
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setMaxPoolSize(maxPoolSize);
    executor.setKeepAliveSeconds(keepAliveSeconds);
    executor.setThreadNamePrefix("OpenBatonAsyncTask-");
    executor.initialize();
    return executor;
  }

  public int getMaxPoolSize() {
    return maxPoolSize;
  }

  public void setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
  }

  public int getCorePoolSize() {
    return corePoolSize;
  }

  public void setCorePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
  }

  public int getQueueCapacity() {
    return queueCapacity;
  }

  public void setQueueCapacity(int queueCapacity) {
    this.queueCapacity = queueCapacity;
  }

  public int getKeepAliveSeconds() {
    return keepAliveSeconds;
  }

  public void setKeepAliveSeconds(int keepAliveSeconds) {
    this.keepAliveSeconds = keepAliveSeconds;
  }
}
