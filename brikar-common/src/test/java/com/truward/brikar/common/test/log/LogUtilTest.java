package com.truward.brikar.common.test.log;

import com.truward.brikar.common.log.LogUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link com.truward.brikar.common.log.LogUtil}.
 *
 * @author Alexander Shabanov
 */
public final class LogUtilTest {

  @Test
  public void shouldReturnSameString() {
    // Given:
    final String value = "abc";

    // When:
    final Object enc = LogUtil.encodeString(value);

    // Then:
    assertTrue("Same reference expected", value == enc);
  }

  @Test
  public void shouldReturnNewString() {
    // Given:
    final String value = "a =,c";

    // When:
    final String enc = LogUtil.encodeString(value);

    // Then:
    assertEquals("a%20%3d%2cc", enc);
  }
}
