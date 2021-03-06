package com.truward.brikar.common.log;

import com.truward.brikar.common.log.metric.Metrics;
import com.truward.brikar.common.log.metric.MetricsCollection;
import com.truward.brikar.common.log.metric.StandardMetricsCollection;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

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
 * Metrics may contain:
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
 *   <li><code>@metric1 op=MeasureUsedToFreeMemRatio, cnt=32</code> - it may mean, that an operation to measure a ratio
 *   of used to free memory has been attempted and its result is 32%</li>
 *   <li><code>@metric1 op=UserService.getUserById, tDelta=125</code> - it may mean, that operation to get user by
 *   ID has been executed and it took 125 milliseconds to complete</li>
 * </ul>
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class LogUtil {
  private LogUtil() {}

  /**
   * A name, under which a request vector is known as one of the logger attributes.
   * <p>
   * Request vector is an identifier that uniquely identifies particular service request, where did it come from,
   * as well as its place in the bigger inter-service call graph.
   * </p>
   *
   * See also {@link org.slf4j.MDC}.
   * See also {@link com.truward.brikar.common.tracking.TrackingHttpHeaderNames#REQUEST_VECTOR}.
   */
  public static final String REQUEST_VECTOR = "RV";

  private static final ThreadLocal<MetricsCollection> METRICS_COLLECTION = new ThreadLocal<>();

  /**
   * Maximum size of request vector.
   */
  public static final int MAX_REQUEST_VECTOR_LENGTH = 4096;

  @Nullable
  public static MetricsCollection getLocalMetricsCollection() {
    return METRICS_COLLECTION.get();
  }

  public static MetricsCollection getOrCreateLocalMetricsCollection() {
    MetricsCollection result = getLocalMetricsCollection();
    if (result == null) {
      result = new StandardMetricsCollection();
      setLocalMetricsCollection(result);
    }
    return result;
  }

  public static void setLocalMetricsCollection(@Nullable MetricsCollection value) {
    METRICS_COLLECTION.set(value);
  }

  public static void logInfo(Metrics metrics, Logger log) {
    final MetricsCollection metricsCollection = new StandardMetricsCollection();
    metricsCollection.add(metrics);
    log.info(metricsCollection.toString());
  }

  public static void logInfo(MetricsCollection metricsCollection, Logger log) {
    log.info(metricsCollection.toString());
  }

  public static void logAndResetLocalMetricsCollection(Logger log, @Nullable MetricsCollection newMetricsCollection) {
    final MetricsCollection metricsCollection = getLocalMetricsCollection();
    if (metricsCollection != null) {
      logInfo(metricsCollection, log);
    }
    setLocalMetricsCollection(newMetricsCollection);
  }

  public static void logAndResetLocalMetricsCollection(Logger log) {
    logAndResetLocalMetricsCollection(log, null);
  }

  /**
   * Propagates metrics entry to the local metrics collection or (if unavailable) logs immediately
   *
   * @param metrics Metrics object to log
   * @param log Target logger to use if local metrics collection is not available
   */
  public static void propagateOrLogInfo(Metrics metrics, Logger log) {
    final MetricsCollection metricsCollection = propagate(metrics);
    if (metricsCollection == null) {
      logInfo(metrics, log);
    }
  }

  /**
   * Propagates metrics entry to the thread local metrics collection, does nothing if local metrics collection
   * is missing.
   *
   * @param metrics Metrics to capture
   * @return Local metrics collection or null
   */
  @Nullable
  public static MetricsCollection propagate(Metrics metrics) {
    final MetricsCollection metricsCollection = getLocalMetricsCollection();
    if (metricsCollection != null) {
      metricsCollection.add(metrics);
    }

    return metricsCollection;
  }

  /**
   * Validates, that passed request vector is valid. This function is used to prevent the potential attacker to send
   * garbage request vectors into the service.
   * If passed request vector is invalid, it should be discarded.
   *
   * @param requestVector Request vector to validate
   * @return True, if passed request vector is valid, false otherwise.
   */
  public static boolean isValidRequestVector(@Nullable String requestVector) {
    if (requestVector == null || requestVector.isEmpty() || requestVector.length() > MAX_REQUEST_VECTOR_LENGTH) {
      return false;
    }

    // verify, that each character is within the allowed bounds
    for (int i = 0; i < requestVector.length(); ++i) {
      final char ch = requestVector.charAt(i);
      if (ch <= 32 || ch >= 127) {
        return false;
      }
    }

    return true;
  }

  //
  // Parameters in logging statements.
  //

  /**
   * Preceding keyword, which presence indicates that a special entry will be coded afterwards.
   * There should be one and only one metric entry per logging statement.
   *
   * One identifies record version for possible future extension with the new fields.
   */
  public static final String METRIC_ENTRY = "@metric1";

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
   * A name of the attribute, corresponding to operation start time (unix time, in milliseconds).
   * Usually recorded as one of the metric attributes.
   */
  public static final String START_TIME = "tStart";

  /**
   * A name of the attribute, corresponding to time spent (in milliseconds).
   * Usually recorded as one of the metric attributes.
   */
  public static final String TIME_DELTA = "tDelta";

  /**
   * A name of an optional attribute which indicates whether or not operation failed.
   * Usually recorded as one of the metric attributes.
   */
  public static final String FAILED = "failed";

  /**
   * HTTP method (invocation type)
   */
  public static final String VERB = "verb";

  /**
   * Http Response Code (integer), e.g. 200 - 'OK', 404 - 'Not Found', etc.
   */
  public static final String RESPONSE_CODE = "responseCode";

  /**
   * Http URL, e.g. /something/like/that
   */
  public static final String URL = "url";

  /**
   * Request vector, that contains in the service call response.
   */
  public static final String RESPONSE_REQUEST_VECTOR = "responseRV";

  /**
   * Value for entry that should be used if corresponding value is omitted.
   */
  public static final String UNKNOWN_VALUE = "?";


  //
  // Helper methods
  //

  /**
   * Encodes a value, so that it won't contain spaces, commas and equal signs.
   *
   * @param value Value to be encoded
   * @return Encoded value or same value if passed argument does not contain whitespace, comma or equals sign
   */
  public static String encodeString(String value) {
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
}
