package com.truward.brikar.maintenance.log.processor;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.maintenance.log.CommaSeparatedValueParser;
import com.truward.brikar.maintenance.log.Severity;
import com.truward.brikar.maintenance.log.message.LogMessage;
import com.truward.brikar.maintenance.log.message.MaterializedLogMessage;
import com.truward.brikar.maintenance.log.message.MultiLinePartLogMessage;
import com.truward.brikar.maintenance.log.message.NullLogMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Message processor that converts a line into a message.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class LogMessageProcessor {

  public static final Pattern RECORD_PATTERN = Pattern.compile(
      "^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}) " + // date+time
          "(\\p{Upper}+) " + // severity
          "([\\w\\p{Punct}]+) " + // class name
          "((?:[\\w]+=[\\w\\+/.\\$]+)(?:, (?:[\\w]+=[\\w\\+/\\.\\$]+))*)? " + // variables
          "\\[[\\w\\p{Punct}&&[^\\]]]+\\] " + // thread ID
          "(.+)" + // message
          "$"
  );

  private static final String METRIC_MARKER = LogUtil.METRIC_ENTRY + ' ';

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final DateFormat dateFormat;

  public LogMessageProcessor() {
    this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
    this.dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  public LogMessage parse(String line, LogMessage previousMessage) {
    if (previousMessage.hasMetrics() && line.startsWith("\t")) {
      // special case: this line is a continuation of metrics entry started on the previous line
      final MaterializedLogMessage logMessage = new MaterializedLogMessage(previousMessage.getUnixTime(),
          previousMessage.getSeverity(), line);
      addAttributesFromMetrics(logMessage, line.substring(1));
      return logMessage;
    }

    final Matcher matcher = RECORD_PATTERN.matcher(line);
    if (!matcher.matches()) {
      return new MultiLinePartLogMessage(line);
    }

    if (matcher.groupCount() < 5) {
      log.error("Count of groups is not six: actual={} for line={}", matcher.groupCount(), line);
      return NullLogMessage.INSTANCE; // should not happen
    }

    final Date date;
    try {
      date = dateFormat.parse(matcher.group(1));
    } catch (ParseException e) {
      log.error("Malformed date in line={}", line, e);
      return NullLogMessage.INSTANCE; // should not happen
    }

    final Severity severity = Severity.fromString(matcher.group(2), Severity.WARN);

    final MaterializedLogMessage logMessage = new MaterializedLogMessage(date.getTime(), severity, line);
    addAttributesFromVariables(logMessage, matcher.group(4));

    final String message = matcher.group(5);
    if (message != null) {
      final int metricIndex = message.indexOf(METRIC_MARKER);
      if (metricIndex >= 0) {
        addAttributesFromMetrics(logMessage, message.substring(metricIndex + METRIC_MARKER.length()));
      }
    }

    return logMessage;
  }

  //
  // Private
  //

  private void addAttributesFromMetrics(MaterializedLogMessage logMessage, String metricBody) {
    putAllAttributes(logMessage, new CommaSeparatedValueParser(metricBody).readAsMap());
  }

  private void addAttributesFromVariables(MaterializedLogMessage logMessage, @Nullable String variables) {
    if (variables != null) {
      putAllAttributes(logMessage, new CommaSeparatedValueParser(variables).readAsMap());
    }
  }

  private void putAllAttributes(MaterializedLogMessage logMessage, Map<String, String> vars) {
    for (final Map.Entry<String, String> entry : vars.entrySet()) {
      final String key = entry.getKey();
      final Object value;
      if (LogUtil.TIME_DELTA.equals(key)) {
        value = Long.parseLong(entry.getValue());
      } else if (LogUtil.START_TIME.equals(key)) {
        value = Long.parseLong(entry.getValue());
      } else if (LogUtil.COUNT.equals(key)) {
        value = Long.parseLong(entry.getValue());
      } else if (LogUtil.FAILED.equals(key)) {
        value = Boolean.valueOf(entry.getValue());
      } else {
        value = entry.getValue();
      }
      logMessage.putAttribute(key, value);
    }
  }
}
