package com.truward.brikar.common.test.log;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.test.util.TestLoggerProvider;
import org.junit.Test;
import org.slf4j.Logger;

import static org.junit.Assert.*;

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
  public void shouldVerifyRequestVector() {
    assertTrue(LogUtil.isValidRequestVector("1"));
    assertTrue(LogUtil.isValidRequestVector(
        "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"));
    assertFalse(LogUtil.isValidRequestVector(null));
    assertFalse(LogUtil.isValidRequestVector(""));
    assertFalse(LogUtil.isValidRequestVector(" "));
    assertFalse(LogUtil.isValidRequestVector(" 1"));
    assertFalse(LogUtil.isValidRequestVector("1 "));

    // huge request IDs should be discarded
    final int hugeRequestVectorLength = LogUtil.MAX_REQUEST_VECTOR_LENGTH + 1;
    final StringBuilder hugeRequestVectorBuilder = new StringBuilder(hugeRequestVectorLength);
    for (int i = 0; i < hugeRequestVectorLength; ++i) {
      hugeRequestVectorBuilder.append('1');
    }
    assertFalse(LogUtil.isValidRequestVector(hugeRequestVectorBuilder.toString()));
  }

  @Test
  public void shouldRecordEmptyLapse() {
    // Given:
    final TestLoggerProvider loggerProvider = new TestLoggerProvider();
    final Logger logger = loggerProvider.getLogger();

    // When:
    LogUtil.logInfo(new SimpleLapse(), logger);

    // Then:
    final String contents = loggerProvider.getRawLogContents();
    assertTrue(contents.endsWith("@metric1 op=?" + System.lineSeparator()));
  }
}
