package com.truward.brikar.common.test.executor;

import com.truward.brikar.common.executor.StandardThreadParametersBinder;
import com.truward.brikar.common.executor.ThreadLocalPropagatingTaskExecutor;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.test.util.TestLoggerProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import java.util.Arrays;
import java.util.UUID;
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
        Arrays.asList(
            StandardThreadParametersBinder.REQUEST_VECTOR,
            StandardThreadParametersBinder.METRICS_COLLECTION));

    MDC.clear(); // clear all parameters
  }

  @After
  public void disposeLogs() throws Exception {
    loggerProvider.destroy();
  }

  @Test
  public void shouldRecordStandardParameters() throws Exception {
    // Given:
    final String requestVector = UUID.randomUUID().toString();
    final Logger log = loggerProvider.getLogger();

    // When:
    MDC.put(LogUtil.REQUEST_VECTOR, requestVector);
    final Future<Integer> future = taskExecutor.submit(() -> {
      LogUtil.logInfo(new SimpleLapse()
          .setOperation("ReturnOne")
          .setCount(1), log);
      return 1;
    });

    // Then:
    assertEquals(Integer.valueOf(1), future.get());
    final String rawContents = loggerProvider.getRawLogContents();
    assertTrue(rawContents.contains("@metric1 op=ReturnOne, cnt=1"));
    assertTrue(rawContents.contains(requestVector));
  }
}
