package com.brikar.persistence.util;

import javax.annotation.Nonnull;

/**
 * Helper for service code on top of persistence layer in the application:
 * <ul>
 *   <li>Transforms IDs to Tokens and vice versa.</li>
 *   <li>Checks limit</li>
 * </ul>
 *
 * @author Alexander Shabanov
 */
public final class ConcealUtil {

  private static final String LONG_NUM_PREFIX = "OL-";
  private static final String INVALID_OFFSET_TOKEN_MESSAGE = "Invalid offset token";

  /**
   * Transforms given offset token which is expected to contain encoded long value from string to long.
   * Encoded value is expected to be previously retrieved by using {@link #conceal(long)} method.
   *
   * @param concealedToken Concealed token
   * @return Long value, from the given offset token
   */
  public static long parseLong(@Nonnull String concealedToken) {
    try {
      if (!concealedToken.startsWith(LONG_NUM_PREFIX)) {
        throw new IllegalArgumentException(INVALID_OFFSET_TOKEN_MESSAGE);
      }
      return Long.parseLong(concealedToken.substring(LONG_NUM_PREFIX.length()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(INVALID_OFFSET_TOKEN_MESSAGE, e);
    }
  }

  /**
   * Encodes given long value to concealed token. Encoded value can be transformed back to the long value by using
   * {@link #parseLong(String)} method.
   *
   * @param id Long value, to be converted to offset token.
   * @return Concealed token
   */
  @Nonnull
  public static String conceal(long id) {
    //noinspection StringBufferReplaceableByString
    final StringBuilder result = new StringBuilder(22);
    result.append(LONG_NUM_PREFIX);
    result.append(id);
    return result.toString();
  }
}
