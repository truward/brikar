package com.truward.brikar.common.test.log;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.log.metric.MetricsCollection;
import com.truward.brikar.common.test.util.TestLoggerProvider;
import com.truward.time.TimeSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;
import static com.truward.brikar.common.test.log.support.TestServices.*;

/**
 * Tests for {@link com.truward.brikar.common.log.aspect.StandardLapseLoggerAspect}.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "/spring/LapseLoggerAspectTest-context.xml")
public final class LapseLoggerAspectTest {
  @Resource TimeSource timeSource;
  @Resource(name = "test.mock.calcService") CalcService mockCalcService;
  @Resource(name = "test.real.calcService") CalcService realCalcService;
  @Resource(name = "test.real.calcService2") CalcService realCalcService2;
  @Resource TestLoggerProvider loggerProvider;
  private final String sep = System.lineSeparator();

  @Before
  public void initMocks() {
    reset(mockCalcService);

    when(timeSource.getTimeUnit()).thenReturn(TimeUnit.MILLISECONDS);
    loggerProvider.reset();
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
    final String logContent = loggerProvider.getRawLogContents();
    assertTrue(logContent.endsWith("@metric1 op=CalcService.plus, tStart=1000, tDelta=200" + sep));
  }

  @Test
  public void shouldLogFailedOperation() {
    // Given:
    when(timeSource.currentTime())
        .thenReturn(1000L) // 1st time
        .thenReturn(1001L); // 2nd time
    when(mockCalcService.add(1, 2)).thenThrow(new IllegalArgumentException());

    // When:
    try {
      realCalcService.add(1, 2);
      fail("Calc service should throw an exception");
    } catch (IllegalArgumentException ignored) {
      // OK
    }

    // Then:
    final String logContent = loggerProvider.getRawLogContents();
    assertTrue(logContent.endsWith("@metric1 op=CalcService.plus, tStart=1000, tDelta=1, failed=true" + sep));
  }

  @Test
  public void shouldNotLogAnyEntriesForNonAnnotatedMethods() {
    // Given:
    when(timeSource.currentTime()).thenReturn(1000L);
    when(mockCalcService.add(1, 2)).thenReturn(3);

    // When:
    final int result = realCalcService2.add(1, 2); // class without @Service
    realCalcService.bar(); // method without @LogRegula

    // Then:
    assertEquals(3, result);
    final String logContent = loggerProvider.getRawLogContents();
    assertTrue(logContent.isEmpty());
  }

  @Test
  public void shouldInferPlace() {
    // Given:
    when(timeSource.currentTime()).thenReturn(1000L);

    // When:
    realCalcService.foo();

    // Then:
    final String logContent = loggerProvider.getRawLogContents();
    assertTrue(logContent.contains("op=CalcService.foo"));
  }

  @Test
  public void shouldChainMetrics() {
    // Given:
    when(timeSource.currentTime())
        .thenReturn(1000L) // 1st time
        .thenReturn(1200L); // 2nd time
    when(mockCalcService.add(1, 2)).thenReturn(3);

    // When:
    final MetricsCollection metricsCollection = LogUtil.getOrCreateLocalMetricsCollection();
    metricsCollection.add(new SimpleLapse().setOperation("TopLevel")
        .setStartTime(900)
        .setEndTime(1300));

    final int result = realCalcService.add(1, 2);

    LogUtil.logAndResetLocalMetricsCollection(loggerProvider.getLogger());

    // Then:
    assertEquals(3, result);
    final String logContent = loggerProvider.getRawLogContents();
    assertTrue(logContent.endsWith("@metric1 op=TopLevel, tStart=900, tDelta=400" + sep +
        "\top=CalcService.plus, tStart=1000, tDelta=200" + sep));
  }
}
