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
   * Header name, that should store request ID.
   * It may also include optional components separated by dots.
   * These components identify nested requests to make it easier to populate request graph.
   *
   * Once retrieved the corresponding value should be stored in {@link org.slf4j.MDC} context.
   * See also {@link com.truward.brikar.common.log.LogUtil#REQUEST_ID}.
   */
  public static final String REQUEST_ID = "Request-ID";

  public static String getNestedRequestId(String requestId, long suffix) {
    if (suffix < 0) {
      throw new IllegalArgumentException("suffix");
    }

    if (!StringUtils.hasLength(requestId)) {
      throw new IllegalArgumentException("requestId");
    }

    // allocate string builder with estimated size of the resultant request
    final int numberOfHexLeadingZeros = Long.numberOfLeadingZeros(suffix) / 4;
    final int suffixSize = 16 - numberOfHexLeadingZeros;

    int offset = requestId.length();
    final char[] result = new char[offset + 1 + suffixSize];

    // first, copy original request ID
    for (int i = 0; i < offset; ++i) {
      result[i] = requestId.charAt(i);
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

  public static String getNestedRequestId(String requestId) {
    return getNestedRequestId(requestId, REQUEST_ID_COUNTER.incrementAndGet());
  }

  public static RequestIdParts parseRequestId(String requestId) {
    if (!StringUtils.hasLength(requestId)) {
      throw new IllegalArgumentException("requestId");
    }

    int nextDot = requestId.indexOf('.');
    final String coreRequestId = nextDot >= 0 ? requestId.substring(0, nextDot) : requestId;
    if (coreRequestId.isEmpty()) {
      throw new IllegalArgumentException("requestId");
    }

    final List<Long> suffixes = new ArrayList<>();
    while (nextDot > 0) {
      final int start = nextDot + 1;
      nextDot = requestId.indexOf('.', start);
      final String suffixStr = nextDot > 0 ? requestId.substring(start, nextDot) : requestId.substring(start);
      if (suffixStr.isEmpty()) {
        throw new IllegalArgumentException("requestId");
      }

      suffixes.add(Long.parseLong(suffixStr, 16));
    }

    return new RequestIdParts(coreRequestId, suffixes);
  }

  public static final class RequestIdParts {
    private final String requestId;
    private final List<Long> suffixes;

    public RequestIdParts(String requestId, List<Long> suffixes) {
      this.requestId = requestId;
      this.suffixes = Collections.unmodifiableList(Arrays.asList(suffixes.toArray(new Long[suffixes.size()])));
    }

    public String getRequestId() {
      return requestId;
    }

    public List<Long> getSuffixes() {
      return suffixes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof RequestIdParts)) return false;

      RequestIdParts that = (RequestIdParts) o;

      return getRequestId().equals(that.getRequestId()) && getSuffixes().equals(that.getSuffixes());

    }

    @Override
    public int hashCode() {
      int result = getRequestId().hashCode();
      result = 31 * result + getSuffixes().hashCode();
      return result;
    }

    @Override
    public String toString() {
      final StringBuilder builder = new StringBuilder(requestId.length() + suffixes.size() * 17);
      builder.append(getRequestId());
      for (final Long suffix : suffixes) {
        builder.append('.').append(Long.toHexString(suffix));
      }
      return builder.toString();
    }
  }
}
