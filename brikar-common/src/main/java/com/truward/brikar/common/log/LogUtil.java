package com.truward.brikar.common.log;

import com.truward.brikar.common.log.lapse.Lapse;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * Common constants for logging.
 * These constants should be used to provide more information in a form of structured records in the log files.
 * <br>
 * Certain arguments should come in a form of {@link org.slf4j.MDC} variables, the others may come in a form of
 * metric entries.
 * <br>
 * The metric entry is a substring in log message with specific format: it should start with a constant
 * {@link #METRIC_ENTRY} and then contain comma+space separated entries where key and value are separated by
 * equals sign.
 * <br>
 * Each log record may contain none or exactly one metric entry which should be the last one in log message.
 * It is completely up to the developer to define semantics behind metric attributes, but general convention is as
 * follows:
 * Metric may contain:
 * <ul>
 *   <li>Operation name</li>
 *   <li>Optional time delta (how much time this operation took, in milliseconds)</li>
 *   <li>Optional flag, that indicates whether or not operation is failed. Absence of this flag indicates that
 *   operation succeeded</li>
 *   <li>Optional integer value that represents a count of something</li>
 * </ul>
 * <br>
 * Example of metric entries:
 * <ul>
 *   <li><code>@metric op=MeasureUsedToFreeMemRatio, cnt=32</code> - it may mean, that an operation to measure a ratio
 *   of used to free memory has been attempted and its result is 32%</li>
 *   <li><code>@metric op=UserService.getUserById, tDelta=125</code> - it may mean, that operation to get user by
 *   ID has been executed and it took 125 milliseconds to complete</li>
 * </ul>
 *
 * @author Alexander Shabanov
 */
public final class LogUtil {
  private LogUtil() {}

  /**
   * A name, under which a request ID is known as one of the logger attributes.
   * <p>
   * Request ID is an identifer that uniquely identifies particular service request.
   * </p>
   *
   * See also {@link org.slf4j.MDC}.
   * See also {@link #ORIGINATING_REQUEST_ID}.
   * See also {@link com.truward.brikar.common.tracking.TrackingHttpHeaderNames#REQUEST_ID}.
   */
  public static final String REQUEST_ID = "rid";

  /**
   * A name, under which an originating request ID is known as one of the logger attributes.
   * <p>
   * Originating Request ID is an identifier that identifies first service request (usually customer's web request),
   * which eventually resulted in particular service request. All service requests related to handling originating
   * service request should have the same value in originating request id.
   * </p>
   *
   * See also {@link org.slf4j.MDC}.
   * See also {@link #REQUEST_ID}.
   * See also {@link com.truward.brikar.common.tracking.TrackingHttpHeaderNames#ORIGINATING_REQUEST_ID}.
   */
  public static final String ORIGINATING_REQUEST_ID = "oid";

  //
  // Parameters in logging statements.
  //

  /**
   * Preceding keyword, which presence indicates that a special entry will be coded afterwards.
   * There should be one and only one metric entry per logging statement.
   */
  public static final String METRIC_ENTRY = "@metric";

  /**
   * A name, under which an operation should be known.
   * Usually recorded as one of the metric attributes.
   */
  public static final String OPERATION = "op";

  /**
   * A name, under which an integer count of something, related to the operation should be known.
   * For example, if operation is to measure used/free memory ratio, this field should contain percentage.
   */
  public static final String COUNT = "cnt";

  /**
   * A name, under which a time should be known.
   * Usually recorded as one of the metric attributes.
   */
  public static final String TIME_DELTA = "tDelta";

  /**
   * A name of an optional attribute which indicates whether or not operation failed.
   * Usually recorded as one of the metric attributes.
   */
  public static final String FAILED = "failed";

  /**
   * Value for entry that should be used if corresponding value is omitted.
   */
  public static final String UNKNOWN_VALUE = "?";


  //
  // Helper methods
  //

  public static void writeLapse(@Nonnull Logger log, @Nonnull String operation, long timeDeltaMillis, boolean failed) {
    log.info(failed ? SHORT_FAILED_LAPSE_FORMAT : SHORT_LAPSE_FORMAT, encodeString(operation), timeDeltaMillis);
  }

  public static void writeLapse(@Nonnull Logger log, @Nonnull Lapse lapse) {
    writeLapse(log, lapse.getOperation(), lapse.getTimeDeltaMillis(), lapse.isFailed());
  }

  public static void writeCount(@Nonnull Logger log, @Nonnull String operationName, int count) {
    log.info(COUNT_METRIC_HEADING, encodeString(operationName), count);
  }

  /**
   * encodes a value, so that it won't contain spaces, commas and equal signs.
   *
   * @param value Value to be encoded
   * @return Encoded value or same value if passed argument does not contain whitespace, comma or equals sign
   */
  @Nonnull
  public static String encodeString(@Nonnull String value) {
    int estimatedSize = 0;
    final int len = value.length();

    // estimate output string size to find out whether encoding is required and avoid reallocations in string builder
    for (int i = 0; i < len; ++i) {
      final char ch = value.charAt(i);
      if (ch <= ' ' || ch == ',') {
        estimatedSize += 3;
        continue;
      }

      ++estimatedSize;
    }

    if (value.length() == estimatedSize) {
      return value; // return value as is - it does not contain any special characters
    }

    final StringBuilder builder = new StringBuilder(estimatedSize);
    for (int i = 0; i < len; ++i) {
      final char ch = value.charAt(i);
      if (ch <= ' ') {
        builder.append("%20");
        continue;
      }

      if (ch == ',') {
        builder.append("%2c");
        continue;
      }

      builder.append(ch);
    }

    return builder.toString();
  }

  //
  // Private
  //

  public static final String METRIC_HEADING = METRIC_ENTRY + " " + OPERATION + "={}";

  public static final String COUNT_METRIC_HEADING = METRIC_HEADING + ", " + COUNT + "={}";

  public static final String LAPSE_HEADING = METRIC_ENTRY + " " + OPERATION + "={}, " + TIME_DELTA + "={}";

  public static final String SHORT_LAPSE_FORMAT = LAPSE_HEADING;

  public static final String SHORT_FAILED_LAPSE_FORMAT = LAPSE_HEADING + ", " + FAILED + "=true";
}
