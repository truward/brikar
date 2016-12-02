package com.truward.brikar.common.test.log;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.test.util.TestLoggerProvider;
import com.truward.brikar.common.time.TimeSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

/**
 * @author Alexander Shabanov
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
    assertTrue(logContent.contains("@metric1 op=CalcService.plus, tDelta=200" + sep));
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
    assertTrue(logContent.contains("@metric1 op=CalcService.plus, tDelta=1, failed=true" + sep));
  }

  @Test
  public void shouldNotLogAnyEntriesForNonServiceBean() {
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


  //
  // Helper classes
  //

  public interface CalcService {
    int add(int x, int y);

    void foo();

    void bar();
  }

  @Service
  public static final class TestCalcService implements CalcService {
    final CalcService calcService;

    public TestCalcService(CalcService calcService) {
      this.calcService = calcService;
    }

    @LogLapse("CalcService.plus")
    @Override
    public int add(int x, int y) {
      return calcService.add(x, y);
    }

    @LogLapse
    @Override
    public void foo() {
      calcService.foo();
    }

    @Override
    public void bar() {
      calcService.bar();
    }
  }

  public static final class TestCalcService2 implements CalcService {
    final CalcService calcService;

    public TestCalcService2(CalcService calcService) {
      this.calcService = calcService;
    }

    @LogLapse
    @Override
    public int add(int x, int y) {
      return calcService.add(x, y);
    }

    @LogLapse
    @Override
    public void foo() {
      calcService.foo();
    }

    @LogLapse
    @Override
    public void bar() {
      calcService.bar();
    }
  }
}
