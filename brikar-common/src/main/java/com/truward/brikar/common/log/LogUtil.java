package com.truward.brikar.common.log;

import com.truward.brikar.common.log.lapse.Lapse;
import org.slf4j.Logger;
import org.slf4j.MDC;

/**
 * Common constants for logging.
 *
 * @author Alexander Shabanov
 */
public final class LogUtil {
  private LogUtil() {}

  //
  // Global logging constants.
  //

  public static final String OPERATION_ID = "oid";

  //
  // Parameters in logging statements.
  //

  public static final String REGULA_ENTRY = "@lapse";

  public static final String PLACE = "place";

  public static final String TIME_DELTA = "timeDelta";

  //
  // Default values
  //

  public static final String UNKNOWN_PLACE = "unknown";


  //
  // Helper methods
  //

  public static void writeRegula(Logger log, Lapse lapse) {
    if (!log.isInfoEnabled()) {
      return;
    }

    log.info(lapse.isFailed() ? SHORT_FAILED_REGULA_FORMAT : SHORT_REGULA_FORMAT,
        lapse.getPlace(), lapse.getTimeDeltaMillis());
  }

  public static void putOperationIdToMdc(String operationId) {
    MDC.put(LogUtil.OPERATION_ID, operationId);
  }

  public static void removeOperationIdFromMdc() {
    MDC.remove(LogUtil.OPERATION_ID);
  }

  //
  // Private
  //

  private static final String REGULA_HEADING = REGULA_ENTRY + "={" + PLACE + "={}, " + TIME_DELTA + "={}";

  private static final String SHORT_REGULA_FORMAT = REGULA_HEADING + "}";

  private static final String SHORT_FAILED_REGULA_FORMAT = REGULA_HEADING + ", failed=true}";
}
