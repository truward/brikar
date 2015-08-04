package com.truward.brikar.common.log.lapse;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.time.TimeSource;
import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class SimpleLapse implements Lapse {
  private String operation = LogUtil.UNKNOWN_VALUE;
  private long startTime = -1L;
  private long timeDelta = -1L;
  private boolean failed;

  public void setOperation(@Nonnull String value) {
    this.operation = value;
  }

  public void setStartTime(long timeMillis) {
    this.startTime = timeMillis;
  }

  public void setStartTime(@Nonnull TimeSource timeSource) {
    Assert.notNull(timeSource, "timeSource");
    setStartTime(timeSource.getTimeUnit().toMillis(timeSource.currentTime()));
  }

  public void setTimeDelta(long timeMillis) {
    this.timeDelta = timeMillis;
  }

  public void setEndTime(long timeMillis) {
    if (startTime < 0) {
      throw new IllegalStateException("endTime can be set if and only if startTime has been set before");
    }
    setTimeDelta(timeMillis - startTime);
  }

  public void setEndTime(@Nonnull TimeSource timeSource) {
    Assert.notNull(timeSource, "timeSource");
    setEndTime(timeSource.getTimeUnit().toMillis(timeSource.currentTime()));
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
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
}
