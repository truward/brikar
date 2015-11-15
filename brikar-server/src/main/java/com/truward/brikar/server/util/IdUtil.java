package com.truward.brikar.server.util;

import javax.annotation.Nonnull;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Helper class for creating random identifiers.
 *
 * @author Alexander Shabanov
 */
public final class IdUtil {
  private IdUtil() {}

  private static final int RANDOM_ID_LENGTH = 14;
  private static final char[] RANDOM_ID_CHARS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  @Nonnull
  public static String getRandomId(@Nonnull Random random, int length) {
    final char[] buf = new char[length];
    for (int i = 0; i < length; ++i) {
      buf[i] = RANDOM_ID_CHARS[random.nextInt(RANDOM_ID_CHARS.length)];
    }
    return new String(buf);
  }

  @Nonnull
  public static String getRandomId() {
    return getRandomId(ThreadLocalRandom.current(), RANDOM_ID_LENGTH);
  }
}
