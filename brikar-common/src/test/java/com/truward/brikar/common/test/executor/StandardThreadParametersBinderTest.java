package com.truward.brikar.common.test.executor;

import com.truward.brikar.common.executor.StandardThreadParametersBinder;
import com.truward.brikar.common.executor.ThreadLocalPropagatingTaskExecutor;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.test.util.TestLoggerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for standard thread parameters binder.
 *
 * @author Alexander Shabanov
 */
public final class StandardThreadParametersBinderTest {
  private AsyncTaskExecutor taskExecutor;
  private TestLoggerProvider loggerProvider;

  @Before
  public void init() {
    this.loggerProvider = new TestLoggerProvider();

    this.taskExecutor = new ThreadLocalPropagatingTaskExecutor(
        new SimpleAsyncTaskExecutor("StandardThreadParametersBinderTest-thread"),
        Collections.singletonList(StandardThreadParametersBinder.REQUEST_ID));

    MDC.clear(); // clear all parameters
  }

  @After
  public void disposeLogs() throws Exception {
    loggerProvider.destroy();
  }

  @Test
  public void shouldRecordStandardParameters() throws Exception {
    // Given:
    final String requestId = UUID.randomUUID().toString();
    final Logger log = loggerProvider.getLogger();

    // When:
    MDC.put(LogUtil.REQUEST_ID, requestId);
    final Future<Integer> future = taskExecutor.submit(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        LogUtil.writeCount(log, "ReturnOne", 1);
        return 1;
      }
    });

    // Then:
    assertEquals(Integer.valueOf(1), future.get());
    final String rawContents = loggerProvider.getRawLogContents();
    assertTrue(rawContents.contains("@metric op=ReturnOne, cnt=1"));
    assertTrue(rawContents.contains(requestId));
  }
}
