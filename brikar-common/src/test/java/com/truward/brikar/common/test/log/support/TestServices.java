package com.truward.brikar.common.test.log.support;

import com.truward.brikar.common.log.LogLapse;

/**
 * Services for log aspect testing.
 */
public final class TestServices {
  private TestServices() {}

  public interface CalcService {
    int add(int x, int y);

    void foo();

    void bar();
  }

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

    @Override
    public int add(int x, int y) {
      return calcService.add(x, y);
    }

    @Override
    public void foo() {
      calcService.foo();
    }

    @Override
    public void bar() {
      calcService.bar();
    }
  }
}
