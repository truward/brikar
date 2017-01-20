package com.truward.brikar.common.log.lapse;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.metric.Metrics;
import com.truward.brikar.common.time.TimeSource;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
public final class SimpleLapse implements Lapse {
  private String operation = LogUtil.UNKNOWN_VALUE;
  private long startTime = -1L;
  private long timeDelta = -1L;
  private boolean failed;

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

  public SimpleLapse setTimeDelta(long timeMillis) {
    this.timeDelta = timeMillis;
    return this;
  }

  public SimpleLapse setEndTime(long timeMillis) {
    if (startTime < 0) {
      throw new IllegalStateException("endTime can be set if and only if startTime has been set before");
    }
    return setTimeDelta(timeMillis - startTime);
  }

  public SimpleLapse setEndTime(@Nonnull TimeSource timeSource) {
    return setEndTime(timeSource.getTimeUnit().toMillis(timeSource.currentTime()));
  }

  public SimpleLapse setFailed(boolean failed) {
    this.failed = failed;
    return this;
  }

  @Override
  public long getStartTime() {
    return startTime;
  }

  @Nonnull
  @Override
  public String getOperation() {
    return operation;
  }

  @Override
  public long getTimeDeltaMillis() {
    return timeDelta;
  }

  @Override
  public boolean isFailed() {
    return failed;
  }

  @Override
  public void appendTo(@Nonnull Appendable appendable) throws IOException {
    Metrics.appendValue(appendable, false, LogUtil.OPERATION, getOperation());
    Metrics.appendPositiveValueOrSkip(appendable, true, LogUtil.START_TIME, getStartTime());
    Metrics.appendPositiveValueOrSkip(appendable, true, LogUtil.TIME_DELTA, getTimeDeltaMillis());
    Metrics.appendTrueValueOrSkip(appendable, true, LogUtil.FAILED, isFailed());
  }
}
