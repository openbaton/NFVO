/*
 * Copyright (c) 2015-2018 Open Baton (http://openbaton.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openbaton.nfvo.system;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
  @Scope("prototype")
  public ThreadPoolTaskExecutor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);
    executor.setQueueCapacity(queueCapacity);
    executor.setMaxPoolSize(maxPoolSize > 0 ? maxPoolSize : Integer.MAX_VALUE);
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
