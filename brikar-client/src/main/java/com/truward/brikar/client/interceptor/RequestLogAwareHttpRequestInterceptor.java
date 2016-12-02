package com.truward.brikar.client.interceptor;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import org.apache.http.*;
import org.apache.http.protocol.HttpContext;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * Interceptor, that takes into an account request-related information for later logging in
 * {@link RequestLogAwareHttpResponseInterceptor}. Both share the same context, so response interceptor uses
 * attributes set in here.
 *
 * @author Alexander Shabanov
 */
public final class RequestLogAwareHttpRequestInterceptor implements HttpRequestInterceptor {
  public static final String ATTR_START_TIME = "requestLog.startTime";
  public static final String ATTR_URI = "requestLog.uri";
  public static final String ATTR_METHOD = "requestLog.method";

  @Override
  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
    setOriginatingRequestVector(request);

    final RequestLine requestLine = request.getRequestLine();
    context.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
    context.setAttribute(ATTR_URI, requestLine.getUri());
    context.setAttribute(ATTR_METHOD, requestLine.getMethod());
  }

  //
  // Private
  //

  private static void setOriginatingRequestVector(HttpRequest request) {
    final Header[] existingRequestVectorHeaders = request.getHeaders(TrackingHttpHeaderNames.REQUEST_VECTOR);
    if (existingRequestVectorHeaders.length > 0) {
      return;
    }

    final String sourceRequestVector = MDC.get(LogUtil.REQUEST_VECTOR);

    // set originating request ID for the outgoing request
    if (sourceRequestVector != null) {
      final String nestedRequestVector = TrackingHttpHeaderNames.getNestedRequestVector(sourceRequestVector);
      request.setHeader(TrackingHttpHeaderNames.REQUEST_VECTOR, nestedRequestVector);
    }
  }
}
