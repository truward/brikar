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
    setOriginatingRequestId(request);

    final RequestLine requestLine = request.getRequestLine();
    context.setAttribute(ATTR_START_TIME, System.currentTimeMillis());
    context.setAttribute(ATTR_URI, requestLine.getUri());
    context.setAttribute(ATTR_METHOD, requestLine.getMethod());
  }

  //
  // Private
  //

  private static void setOriginatingRequestId(HttpRequest request) {
    final Header[] existingRequestIdHeaders = request.getHeaders(TrackingHttpHeaderNames.REQUEST_ID);
    if (existingRequestIdHeaders.length > 0) {
      return;
    }

    final String sourceRequestId = MDC.get(LogUtil.REQUEST_ID);

    // set originating request ID for the outgoing request
    if (sourceRequestId != null) {
      final String nestedRequestId = TrackingHttpHeaderNames.getNestedRequestId(sourceRequestId);
      request.setHeader(TrackingHttpHeaderNames.REQUEST_ID, nestedRequestId);
    }
  }
}
