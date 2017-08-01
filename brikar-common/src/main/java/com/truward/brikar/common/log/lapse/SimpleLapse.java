package com.truward.brikar.common.log.lapse;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.metric.Metrics;
import com.truward.time.TimeSource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
public final class SimpleLapse implements Lapse {
  private String operation = LogUtil.UNKNOWN_VALUE;
  private long startTime = -1L;
  private long endTime = -1L;
  private boolean failed;
  private int count = -1;
  private Map<String, String> extraProperties = Collections.emptyMap();

  public SimpleLapse setOperation(@Nonnull String value) {
    this.operation = value;
    return this;
  }

  public SimpleLapse setStartTime(long timeMillis) {
    this.startTime = timeMillis;
    return this;
  }

  public SimpleLapse setStartTime(@Nonnull TimeSource timeSource) {
    return setStartTime(timeSource.getTimeUnit().toMillis(timeSource.currentTime()));
  }

  public SimpleLapse setEndTime(long timeMillis) {
    this.endTime = timeMillis;
    return this;
  }

  public SimpleLapse setEndTime(@Nonnull TimeSource timeSource) {
    return setEndTime(timeSource.getTimeUnit().toMillis(timeSource.currentTime()));
  }

  public SimpleLapse setFailed(boolean failed) {
    this.failed = failed;
    return this;
  }

  public int getCount() {
    return count;
  }

  public SimpleLapse setCount(int value) {
    this.count = value;
    return this;
  }

  public SimpleLapse setProperty(String name, String value) {
    if (extraProperties.isEmpty()) {
      extraProperties = new HashMap<>();
    }
    extraProperties.put(name, value);
    return this;
  }

  public SimpleLapse setProperty(String name, int value) {
    return setProperty(name, Integer.toString(value));
  }

  public SimpleLapse setProperty(String name, long value) {
    return setProperty(name, Long.toString(value));
  }

  public SimpleLapse setProperty(String name, boolean value) {
    return setProperty(name, Boolean.toString(value));
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  @Override
  public long getEndTime() {
    return endTime;
  }

  @Nonnull
  @Override
  public String getOperation() {
    return operation;
  }

  @Override
  public boolean isFailed() {
    return failed;
  }

  @Override
  public void appendTo(@Nonnull Appendable appendable) throws IOException {
    Metrics.appendValue(appendable, false, LogUtil.OPERATION, getOperation());
    Metrics.appendPositiveValueOrSkip(appendable, true, LogUtil.START_TIME, getStartTime());

    if (endTime > 0) {
      if (startTime < 0) {
        Metrics.appendValue(appendable, true, LogUtil.TIME_DELTA, "?");
      } else {
        Metrics.appendPositiveValueOrSkip(appendable, true, LogUtil.TIME_DELTA, endTime - startTime);
      }
    }

    Metrics.appendPositiveValueOrSkip(appendable, true, LogUtil.COUNT, getCount());
    Metrics.appendTrueValueOrSkip(appendable, true, LogUtil.FAILED, isFailed());

    if (extraProperties.isEmpty()) {
      return;
    }
    for (final Map.Entry<String, String> extraPropEntry : extraProperties.entrySet()) {
      Metrics.appendValue(appendable, true, extraPropEntry.getKey(), extraPropEntry.getValue());
    }
  }
}
