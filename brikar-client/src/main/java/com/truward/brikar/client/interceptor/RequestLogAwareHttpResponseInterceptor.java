package com.truward.brikar.client.interceptor;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Apache HTTP client's response interceptor that respects request IDs.
 *
 * @author Alexander Shabanov
 */
public final class RequestLogAwareHttpResponseInterceptor implements HttpResponseInterceptor {
  private final Logger log;

  public RequestLogAwareHttpResponseInterceptor(Logger log) {
    this.log = log;
  }

  @Override
  public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
    final SimpleLapse lapse = new SimpleLapse();

    // capture context parameters
    lapse.setOperation(String.valueOf(context.getAttribute(RequestLogAwareHttpRequestInterceptor.ATTR_URI)));
    lapse.setProperty(LogUtil.VERB,
        String.valueOf(context.getAttribute(RequestLogAwareHttpRequestInterceptor.ATTR_METHOD)));

    final Object startTimeObj = context.getAttribute(RequestLogAwareHttpRequestInterceptor.ATTR_START_TIME);
    if (startTimeObj instanceof Long) {
      lapse.setEndTime(System.currentTimeMillis());
      lapse.setStartTime((Long) startTimeObj);
    }

    // get and validate request ID
    final Header header = response.getLastHeader(TrackingHttpHeaderNames.REQUEST_VECTOR);
    String responseRequestVector = LogUtil.UNKNOWN_VALUE;
    if (header != null) {
      final String val = header.getValue();
      if (LogUtil.isValidRequestVector(val)) {
        responseRequestVector = val;
      }
    }

    final int code = response.getStatusLine().getStatusCode();
    lapse.setProperty(LogUtil.RESPONSE_CODE, code);
    lapse.setProperty(LogUtil.RESPONSE_REQUEST_VECTOR, responseRequestVector);

    LogUtil.propagateOrLogInfo(lapse, log);
  }
}
