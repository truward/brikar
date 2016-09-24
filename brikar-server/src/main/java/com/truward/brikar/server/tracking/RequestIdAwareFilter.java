package com.truward.brikar.server.tracking;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import com.truward.brikar.server.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A filter which performs processing of request ID and originating reqiest ID, such as
 * retrieving and propagating these IDs to the MDC context and putting these IDs to the response headers.
 * See also {@link LogUtil} and {@link TrackingHttpHeaderNames}.
 *
 * @author Alexander Shabanov
 */
public class RequestIdAwareFilter extends OncePerRequestFilter {
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
    final String requestId = IdUtil.getRandomId();
    String originatingRequestId = request.getHeader(TrackingHttpHeaderNames.ORIGINATING_REQUEST_ID);
    if (!LogUtil.isValidRequestId(originatingRequestId)) {
      // reuse request ID if originating request ID has not been passed before
      originatingRequestId = requestId;
    }
    // propagating originating request ID
    MDC.put(LogUtil.ORIGINATING_REQUEST_ID, originatingRequestId);

    // propagate both request ID
    MDC.put(LogUtil.REQUEST_ID, requestId);

    // set headers containing request ID, originating request ID is not needed
    response.setHeader(TrackingHttpHeaderNames.REQUEST_ID, requestId);

    // process request
    if (log == null) {
      filterChain.doFilter(request, response);
    } else {
      String pathInfo = request.getPathInfo(); // can be null if request doesn't have path info
      if (pathInfo != null) {
        pathInfo = LogUtil.encodeString(pathInfo);
      } else {
        pathInfo = "";
      }

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
}
