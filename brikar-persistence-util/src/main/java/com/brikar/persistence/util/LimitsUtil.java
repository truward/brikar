package com.brikar.persistence.util;

/**
 * Helper class that provides methods for checking limits boundaries.
 *
 * @author Alexander Shabanov
 */
public final class LimitsUtil {

  public static final int DEFAULT_LIMIT_MAX_SIZE = 16;

  /**
   * Checks, that limit is less than given size, throws {@link IllegalArgumentException} otherwise.
   * Also checks that limit is not negative.
   *
   * @param limit User-provided limit value
   * @param maxAllowedSize Maximum allowed size for limit.
   * @return Checked limit value.
   */
  public static int checkLimit(int limit, int maxAllowedSize) {
    if (limit > maxAllowedSize) {
      throw new IllegalArgumentException("Size exceeds maximum: " + maxAllowedSize);
    }

    if (limit < 0) {
      throw new IllegalArgumentException("Size can't be negative");
    }

    return limit;
  }

  public static int checkLimit(int limit) {
    return checkLimit(limit, DEFAULT_LIMIT_MAX_SIZE);
  }

  //
  // Private
  //

  private LimitsUtil() {} // Hidden
}
