package com.truward.brikar.server.tracking;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A filter which performs processing of request ID and originating reqiest ID, such as
 * retrieving and propagating these IDs to the MDC context and putting these IDs to the response headers.
 * See also {@link LogUtil} and {@link TrackingHttpHeaderNames}.
 *
 * @author Alexander Shabanov
 */
public class RequestIdAwareFilter extends OncePerRequestFilter {
  private static final int RANDOM_ID_LENGTH = 14;
  private static final char[] RANDOM_ID_CHARS = {
      'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
      'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
      'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
      'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/'
  };

  /**
   * Log Format: operation, method, timeDelta
   */
  private static final String OK_LOG_FORMAT = LogUtil.METRIC_HEADING + ", " + LogUtil.VERB + "={}, " +
      LogUtil.TIME_DELTA + "={}, " + LogUtil.RESPONSE_CODE + "={}";

  private final Logger log;

  public RequestIdAwareFilter() {
    final Logger log = LoggerFactory.getLogger(getClass());
    this.log = log.isTraceEnabled() ? log : null;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
    // get originating request id and current request id
    final String requestId = getRandomRequestId();
    final String originatingRequestId = request.getHeader(TrackingHttpHeaderNames.ORIGINATING_REQUEST_ID);
    if (originatingRequestId != null) {
      // propagating originating request ID if it was passed in headers
      MDC.put(LogUtil.ORIGINATING_REQUEST_ID, originatingRequestId);
    }

    // propagate both request ID
    MDC.put(LogUtil.REQUEST_ID, requestId);

    // set headers containing request ID, originating request ID is not needed
    response.setHeader(TrackingHttpHeaderNames.REQUEST_ID, requestId);

    // process request
    if (log == null) {
      filterChain.doFilter(request, response);
    } else {
      final String pathInfo = LogUtil.encodeString(request.getPathInfo());
      long time = System.currentTimeMillis();
      try {
        filterChain.doFilter(request, response);
      } finally {
        time = System.currentTimeMillis() - time;
        log.info(OK_LOG_FORMAT, pathInfo, request.getMethod(), time, response.getStatus());
      }
    }

    // remove MDC variables
    if (originatingRequestId != null) {
      MDC.remove(LogUtil.ORIGINATING_REQUEST_ID);
    }
    MDC.remove(LogUtil.REQUEST_ID);
  }

  //
  // Private
  //

  @Nonnull
  private static String getRandomRequestId() {
    final Random random = ThreadLocalRandom.current();
    final char[] buf = new char[RANDOM_ID_LENGTH];
    for (int i = 0; i < RANDOM_ID_LENGTH; ++i) {
      buf[i] = RANDOM_ID_CHARS[random.nextInt(RANDOM_ID_CHARS.length)];
    }
    return new String(buf);
  }
}
