package com.brikar.persistence.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ConcealUtil}.
 *
 * @author Alexander Shabanov
 */
public final class ConcealUtilTest {

  @Test
  public void shouldConvertZeroLongValueToAndFromOffsetToken() {
    // Given:
    final long id = 0;

    // When:
    final String token = ConcealUtil.conceal(id);

    // Then:
    assertEquals(id, ConcealUtil.parseLong(token));
  }

  @Test
  public void shouldConvertPositiveLongValueToAndFromOffsetToken() {
    // Given:
    final long id = 123L;

    // When:
    final String token = ConcealUtil.conceal(id);

    // Then:
    assertEquals(id, ConcealUtil.parseLong(token));
  }

  @Test
  public void shouldConvertNegativeLongValueToAndFromOffsetToken() {
    // Given:
    final long id = -321L;

    // When:
    final String token = ConcealUtil.conceal(id);

    // Then:
    assertEquals(id, ConcealUtil.parseLong(token));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailToDecodeLongFromEmptyOffsetToken() {
    ConcealUtil.parseLong("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailToDecodeLongFromMalformedOffsetToken() {
    ConcealUtil.parseLong("A" + ConcealUtil.conceal(123L));
  }
}
