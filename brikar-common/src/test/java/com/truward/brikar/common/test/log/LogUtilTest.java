package com.truward.brikar.common.test.log;

import com.truward.brikar.common.log.LogUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
    assertEquals("a%20=%2cc", enc);
  }

  @Test
  public void shouldVerifyRequestId() {
    assertTrue(LogUtil.isValidRequestId("1"));
    assertTrue(LogUtil.isValidRequestId(
        "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"));
    assertFalse(LogUtil.isValidRequestId(null));
    assertFalse(LogUtil.isValidRequestId(""));
    assertFalse(LogUtil.isValidRequestId(" "));
    assertFalse(LogUtil.isValidRequestId(" 1"));
    assertFalse(LogUtil.isValidRequestId("1 "));

    // huge request IDs should be discarded
    final int hugeRequestIdLength = LogUtil.MAX_REQUEST_ID_LENGTH + 1;
    final StringBuilder hugeRequestIdBuilder = new StringBuilder(hugeRequestIdLength);
    for (int i = 0; i < hugeRequestIdLength; ++i) {
      hugeRequestIdBuilder.append('1');
    }
    assertFalse(LogUtil.isValidRequestId(hugeRequestIdBuilder.toString()));
  }
}
