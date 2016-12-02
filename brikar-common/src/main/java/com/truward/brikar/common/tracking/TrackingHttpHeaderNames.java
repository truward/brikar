package com.truward.brikar.common.tracking;

import org.springframework.util.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Constants, used in HTTP interaction for writing and retrieving request ID and originating request ID.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class TrackingHttpHeaderNames {
  private TrackingHttpHeaderNames() {}

  private static final AtomicLong REQUEST_ID_COUNTER = new AtomicLong();
  private static final String SUFFIX_HEX_CHARS = "0123456789ABCDEF";

  /**
   * Header name, that should store request vector.
   * It may also include optional components separated by dots.
   * These components identify nested requests to make it easier to populate request graph.
   *
   * Once retrieved the corresponding value should be stored in {@link org.slf4j.MDC} context.
   * See also {@link com.truward.brikar.common.log.LogUtil#REQUEST_VECTOR}.
   */
  public static final String REQUEST_VECTOR = "RV";

  public static String getNestedRequestVector(String requestVector, long suffix) {
    if (suffix < 0) {
      throw new IllegalArgumentException("suffix");
    }

    if (!StringUtils.hasLength(requestVector)) {
      throw new IllegalArgumentException("requestVector");
    }

    // allocate string builder with estimated size of the resultant request
    final int numberOfHexLeadingZeros = Long.numberOfLeadingZeros(suffix) / 4;
    final int suffixSize = 16 - numberOfHexLeadingZeros; // 16 is the max size of hex representation of the long number

    int offset = requestVector.length();
    final char[] result = new char[offset + 1 + suffixSize];

    // first, copy original request ID
    for (int i = 0; i < offset; ++i) {
      result[i] = requestVector.charAt(i);
    }

    // then append dot
    result[offset] = '.';

    // then append suffix in hexadecimal form - using this method here to avoid reallocations
    for (offset = result.length - 1; suffix != 0; --offset) {
      final int hexDigit = (int) (suffix % 16);
      suffix = suffix / 16;
      final char ch = SUFFIX_HEX_CHARS.charAt(hexDigit);
      result[offset] = ch;
    }

    return new String(result);
  }

  public static String getNestedRequestVector(String requestVector) {
    return getNestedRequestVector(requestVector, REQUEST_ID_COUNTER.incrementAndGet());
  }

  public static RequestVectorParts parseRequestVector(String requestVector) {
    if (!StringUtils.hasLength(requestVector)) {
      throw new IllegalArgumentException("requestVector");
    }

    int nextDot = requestVector.indexOf('.');
    final String coreRequestVector = nextDot >= 0 ? requestVector.substring(0, nextDot) : requestVector;
    if (coreRequestVector.isEmpty()) {
      throw new IllegalArgumentException("requestVector");
    }

    final List<Long> suffixes = new ArrayList<>();
    while (nextDot > 0) {
      final int start = nextDot + 1;
      nextDot = requestVector.indexOf('.', start);
      final String suffixStr = nextDot > 0 ? requestVector.substring(start, nextDot) : requestVector.substring(start);
      if (suffixStr.isEmpty()) {
        throw new IllegalArgumentException("requestVector");
      }

      suffixes.add(Long.parseLong(suffixStr, 16));
    }

    return new RequestVectorParts(coreRequestVector, suffixes);
  }

  public static final class RequestVectorParts {
    private final String requestVector;
    private final List<Long> suffixes;

    public RequestVectorParts(String requestVector, List<Long> suffixes) {
      this.requestVector = requestVector;
      this.suffixes = Collections.unmodifiableList(Arrays.asList(suffixes.toArray(new Long[suffixes.size()])));
    }

    public String getRequestVector() {
      return requestVector;
    }

    public List<Long> getSuffixes() {
      return suffixes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RequestVectorParts)) return false;

      RequestVectorParts that = (RequestVectorParts) o;

      return getRequestVector().equals(that.getRequestVector()) && getSuffixes().equals(that.getSuffixes());

    }

    @Override
    public int hashCode() {
      int result = getRequestVector().hashCode();
      result = 31 * result + getSuffixes().hashCode();
      return result;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder(requestVector.length() + suffixes.size() * 17);
      builder.append(getRequestVector());
      for (final Long suffix : suffixes) {
        builder.append('.').append(Long.toHexString(suffix));
      }
      return builder.toString();
    }
  }
}
