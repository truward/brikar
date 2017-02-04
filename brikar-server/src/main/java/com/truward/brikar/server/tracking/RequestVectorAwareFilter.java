package com.truward.brikar.server.tracking;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.log.metric.MetricsCollection;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import com.truward.brikar.server.util.IdUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import javax.servlet.*;
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
public class RequestVectorAwareFilter implements Filter {
  private final Logger log = LoggerFactory.getLogger("BrikarRequestLogger");

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.debug("init");
  }

  @Override
  public void doFilter(ServletRequest req,
                       ServletResponse resp,
                       FilterChain filterChain) throws IOException, ServletException {
    final HttpServletRequest request = (HttpServletRequest) req;
    final HttpServletResponse response = (HttpServletResponse) resp;

    // get originating request ID and propagate it to the logging context
    String originatingRequestVector = request.getHeader(TrackingHttpHeaderNames.REQUEST_VECTOR);
    if (!LogUtil.isValidRequestVector(originatingRequestVector)) {
      originatingRequestVector = null;
    }

    if (originatingRequestVector == null) {
      originatingRequestVector = IdUtil.getRandomId();
    }

    MDC.put(LogUtil.REQUEST_VECTOR, originatingRequestVector);

    // set headers containing request ID
    response.setHeader(TrackingHttpHeaderNames.REQUEST_VECTOR, originatingRequestVector);

    // process request
    if (log.isInfoEnabled()) {
      String pathInfo = request.getPathInfo(); // can be null if request doesn't have path info
      if (pathInfo != null) {
        pathInfo = LogUtil.encodeString(pathInfo);
      } else {
        pathInfo = "";
      }

      final SimpleLapse lapse = new SimpleLapse();
      lapse.setStartTime(System.currentTimeMillis());
      final MetricsCollection metricsCollection = LogUtil.getOrCreateLocalMetricsCollection();
      try {
        filterChain.doFilter(request, response);
      } finally {
        lapse.setEndTime(System.currentTimeMillis());

        // This code gets URL pattern associated with the method that handled request
        Object urlPattern = request.getAttribute("org.springframework.web.servlet.HandlerMapping.bestMatchingPattern");
        final String urlOperation = urlPattern != null ? urlPattern.toString() : pathInfo;
        final int operationSizeEstimate = request.getMethod().length() + 1 + urlOperation.length();
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder operation = new StringBuilder(operationSizeEstimate);
        operation.append(request.getMethod()).append('_').append(urlOperation);

        lapse.setOperation(operation.toString());
        lapse.setProperty(LogUtil.VERB, request.getMethod());
        lapse.setProperty(LogUtil.RESPONSE_CODE, response.getStatus());
        lapse.setProperty(LogUtil.URL, pathInfo);
        metricsCollection.add(lapse);

        LogUtil.logAndResetLocalMetricsCollection(log);
      }
    } else {
      filterChain.doFilter(request, response);
    }

    // remove MDC variables
    MDC.remove(LogUtil.REQUEST_VECTOR);
  }

  @Override
  public void destroy() {
    log.debug("destroy");
  }
}
