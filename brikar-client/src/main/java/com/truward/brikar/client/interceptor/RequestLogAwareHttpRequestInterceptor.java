package com.truward.brikar.client.interceptor;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.RequestLine;
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
    final long startTime = System.currentTimeMillis();

    final String originatingRequestId = MDC.get(LogUtil.ORIGINATING_REQUEST_ID);
    if (originatingRequestId != null) {
      request.setHeader(TrackingHttpHeaderNames.ORIGINATING_REQUEST_ID, originatingRequestId);
    }

    final RequestLine requestLine = request.getRequestLine();
    context.setAttribute(ATTR_START_TIME, startTime);
    context.setAttribute(ATTR_URI, requestLine.getUri());
    context.setAttribute(ATTR_METHOD, requestLine.getMethod());
  }
}
