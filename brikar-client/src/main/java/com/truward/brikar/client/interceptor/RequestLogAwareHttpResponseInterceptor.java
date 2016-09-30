package com.truward.brikar.client.interceptor;

import com.truward.brikar.common.log.LogUtil;
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
  /**
   * Layout: operation={}, timeDelta={}, method={}, responseCode={}, responseRid={}
   */
  private static final String LOG_FMT = LogUtil.LAPSE_HEADING + ", " +
      LogUtil.VERB + "={}, " + LogUtil.RESPONSE_CODE + "={}, " + LogUtil.RESPONSE_REQUEST_ID + "={}";

  private final Logger log;

  public RequestLogAwareHttpResponseInterceptor(Logger log) {
    this.log = log;
  }

  @Override
  public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
    // capture context parameters
    final String uri = String.valueOf(context.getAttribute(RequestLogAwareHttpRequestInterceptor.ATTR_URI));
    final String method = String.valueOf(context.getAttribute(RequestLogAwareHttpRequestInterceptor.ATTR_METHOD));
    final Object startTimeObj = context.getAttribute(RequestLogAwareHttpRequestInterceptor.ATTR_START_TIME);
    final long timeDelta;
    if (startTimeObj instanceof Long) {
      timeDelta = System.currentTimeMillis() - (Long) startTimeObj;
    } else {
      timeDelta = -1L; // should never happen
    }

    // get and validate request ID
    final Header header = response.getLastHeader(TrackingHttpHeaderNames.REQUEST_ID);
    String responseRequestId = LogUtil.UNKNOWN_VALUE;
    if (header != null) {
      final String val = header.getValue();
      if (LogUtil.isValidRequestId(val)) {
        responseRequestId = val;
      }
    }

    final int code = response.getStatusLine().getStatusCode();

    if (code >= 200 && code < 300) {
      log.info(LOG_FMT, uri, timeDelta, method, code, responseRequestId);
    } else {
      log.warn(LOG_FMT, uri, timeDelta, method, code, responseRequestId);
    }
  }
}
