package com.truward.brikar.common.tracking;

/**
 * Constants, used in HTTP interaction for writing and retrieving request ID and originating request ID.
 *
 * @author Alexander Shabanov
 */
public final class TrackingHttpHeaderNames {
  private TrackingHttpHeaderNames() {}

  /**
   * Header name, that should store request ID.
   * Once retrieved the corresponding value should be stored in {@link org.slf4j.MDC} context.
   * See also {@link com.truward.brikar.common.log.LogUtil#REQUEST_ID}.
   */
  public static final String REQUEST_ID = "X-Rid";

  /**
   * Header name, that should store originating request ID.
   * Once retrieved the corresponding value should be stored in {@link org.slf4j.MDC} context.
   * See also {@link com.truward.brikar.common.log.LogUtil#ORIGINATING_REQUEST_ID}.
   */
  public static final String ORIGINATING_REQUEST_ID = "X-Oid";
}
