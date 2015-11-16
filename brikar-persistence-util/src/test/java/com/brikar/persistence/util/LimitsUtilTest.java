package com.brikar.persistence.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link LimitsUtil}.
 *
 * @author Alexander Shabanov
 */
public final class LimitsUtilTest {
  private final int maxAllowedValue = LimitsUtil.DEFAULT_LIMIT_MAX_SIZE * 2 + 10;

  @Test
  public void shouldPassLimitCheckForZeroValue() {
    final int value = 0;
    assertEquals(value, LimitsUtil.checkLimit(value));
  }

  @Test
  public void shouldPassLimitCheckForPreMaxValue() {
    final int value = LimitsUtil.DEFAULT_LIMIT_MAX_SIZE;
    assertEquals(value, LimitsUtil.checkLimit(value));
  }

  @Test
  public void shouldPassNonDefaultLimitCheckForPreMaxValue() {
    final int value = maxAllowedValue;
    assertEquals(value, LimitsUtil.checkLimit(value, maxAllowedValue));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailLimitCheckForNegativeValue() {
    LimitsUtil.checkLimit(-1);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailLimitCheckForValueExceedingGivenMaximum() {
    final int value = maxAllowedValue + 1;
    LimitsUtil.checkLimit(value, maxAllowedValue);
  }
}
