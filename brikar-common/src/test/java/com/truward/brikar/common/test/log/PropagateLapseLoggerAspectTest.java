package com.truward.brikar.common.test.log;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.log.metric.Metrics;
import com.truward.brikar.common.log.metric.MetricsCollection;
import com.truward.brikar.common.test.log.support.TestServices.CalcService;
import com.truward.time.TimeSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for {@link com.truward.brikar.common.log.aspect.PropagateLapseLoggerAspectBean}.
 */
@SuppressWarnings("WeakerAccess")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/PropagateLoggerAspectTest-context.xml")
public class PropagateLapseLoggerAspectTest {
  @Resource TimeSource timeSource;
  @Resource AsyncTaskExecutor taskExecutor;
  @Resource(name = "test.mock.calcService") CalcService mockCalcService;
  @Resource(name = "test.real.calcService") CalcService realCalcService;
  @Resource(name = "test.real.calcService2") CalcService realCalcService2;
  private TestMetricsCollection metricsCollection;

  @Before
  public void initMocks() {
    reset(mockCalcService, timeSource);

    when(timeSource.getTimeUnit()).thenReturn(TimeUnit.MILLISECONDS);

    metricsCollection = new TestMetricsCollection();
    LogUtil.setLocalMetricsCollection(metricsCollection);
  }

  @After
  public void cleanup() {
    LogUtil.setLocalMetricsCollection(null);
  }

  @Test
  public void shouldLogMetricEntry() {
    // Given:
    when(timeSource.currentTime())
        .thenReturn(1000L) // 1st time
        .thenReturn(1200L); // 2nd time
    when(mockCalcService.add(1, 2)).thenReturn(3);

    // When:
    final int result = realCalcService.add(1, 2);

    // Then:
    assertEquals(3, result);
    final SimpleLapse lapse = metricsCollection.expectOneEntry(SimpleLapse.class);
    assertEquals(1000, lapse.getStartTime());
    assertEquals(1200, lapse.getEndTime());
    assertEquals("CalcService.plus", lapse.getOperation());
  }

  @Test
  public void shouldNotLogAnyEntriesForNonAnnotatedMethods() {
    // Given:
    when(mockCalcService.add(1, 2)).thenReturn(3);

    // When:
    final int result = realCalcService2.add(1, 2);
    realCalcService.bar();

    // Then:
    assertEquals(3, result);
    assertEquals(Collections.emptyList(), metricsCollection.metricsList);
    verifyZeroInteractions(timeSource);
  }

  @Test
  public void shouldPropagateMetricsInMultithreadedEnvironment() throws ExecutionException, InterruptedException {
    // Given:
    when(timeSource.currentTime())
        .thenReturn(1000L) // 1st time
        .thenReturn(1200L); // 2nd time
    when(mockCalcService.add(1, 2)).thenReturn(3);
    final long masterThreadId = Thread.currentThread().getId();

    // When:
    final Integer result = taskExecutor.submit(() -> {
      assertNotEquals(
          "New task shall be executed in separate thread",
          masterThreadId,
          Thread.currentThread().getId());
      return realCalcService.add(1, 2);
    }).get();
    realCalcService.bar();

    // Then:
    assertEquals(3, result.intValue());

    final SimpleLapse lapse = metricsCollection.expectOneEntry(SimpleLapse.class);
    assertEquals(1000, lapse.getStartTime());
    assertEquals(1200, lapse.getEndTime());
    assertEquals("CalcService.plus", lapse.getOperation());
  }

  //
  // Private
  //

  @ParametersAreNonnullByDefault
  private static final class TestMetricsCollection implements MetricsCollection {
    List<Metrics> metricsList = new ArrayList<>();

    <T extends Metrics> T expectOneEntry(Class<T> metricsClass) {
      assertEquals("Metrics collection shall have only one entry", 1, metricsList.size());
      final Metrics result = metricsList.get(0);
      assertTrue("Metrics should be an instance of " + metricsClass, metricsClass.isInstance(result));
      return metricsClass.cast(result);
    }

    @Override
    public void add(Metrics metrics) {
      metricsList.add(metrics);
    }
  }
}
