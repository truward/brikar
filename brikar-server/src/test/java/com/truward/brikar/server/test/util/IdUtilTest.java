package com.truward.brikar.server.test.util;

import com.truward.brikar.server.util.IdUtil;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link IdUtil}.
 *
 * @author Alexander Shabanov
 */
public final class IdUtilTest {

  @Test
  public void shouldGenerateRandomId() {
    final String id = IdUtil.getRandomId();
    assertNotNull(id);
  }
}
