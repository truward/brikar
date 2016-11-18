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
 *
 * This filter is associated with logger, which produces results under <code>BrikarRequestLogger</code> name.
 *
 * See also {@link LogUtil} and {@link TrackingHttpHeaderNames}.
 *
 * @author Alexander Shabanov
 */
public class RequestIdAwareFilter extends OncePerRequestFilter {
  /**
   * Log Format: operation, method, timeDelta, responseCode, url
   */
  private static final String OK_LOG_FORMAT = LogUtil.METRIC_HEADING + ", " + LogUtil.VERB + "={}, " +
      LogUtil.TIME_DELTA + "={}, " + LogUtil.RESPONSE_CODE + "={}, " + LogUtil.URL + "={}";

  private final Logger log = LoggerFactory.getLogger("BrikarRequestLogger");

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    // get originating request ID and propagate it to the logging context
    String originatingRequestId = request.getHeader(TrackingHttpHeaderNames.REQUEST_ID);
    if (!LogUtil.isValidRequestId(originatingRequestId)) {
      originatingRequestId = null;
    }

    if (originatingRequestId == null) {
      originatingRequestId = IdUtil.getRandomId();
    }

    MDC.put(LogUtil.REQUEST_ID, originatingRequestId);

    // set headers containing request ID
    response.setHeader(TrackingHttpHeaderNames.REQUEST_ID, originatingRequestId);

    // process request
    if (log.isTraceEnabled()) {
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
        // This code gets URL pattern associated with the method that handled request
        Object urlPattern = request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        final String urlOperation = urlPattern != null ? urlPattern.toString() : pathInfo;

        final int operationSizeEstimate = request.getMethod().length() + 1 + urlOperation.length();
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder operation = new StringBuilder(operationSizeEstimate);
        operation.append(request.getMethod()).append('_').append(urlOperation);

        log.info(OK_LOG_FORMAT, operation.toString(), request.getMethod(), time, response.getStatus(), pathInfo);
      }
    } else {
      filterChain.doFilter(request, response);
    }

    // remove MDC variables
    MDC.remove(LogUtil.REQUEST_ID);
  }
}
