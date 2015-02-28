package com.truward.brikar.client.backoff.support;

import com.truward.brikar.client.backoff.BackoffMark;
import com.truward.brikar.client.backoff.BackoffStrategy;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * @author Alexander Shabanov
 */
public final class TruncatedExponentialBackoffStrategy implements BackoffStrategy {
  private long startTimeMillis;
  private long timeDeltaMillis;
  private float multiplier;
  private long maxTimeMillis;

  public TruncatedExponentialBackoffStrategy() {
    setStartTimeMillis(0L);
    setTimeDeltaMillis(50L);
    setMultiplier(2.0f);
    setMaxTimeMillis(1000L);
  }

  public void setStartTimeMillis(long startTimeMillis) {
    if (startTimeMillis < 0L) {
      throw new IllegalArgumentException("Start time should not be negative");
    }

    this.startTimeMillis = startTimeMillis;
  }

  public void setTimeDeltaMillis(long timeDeltaMillis) {
    if (timeDeltaMillis <= 0L) {
      throw new IllegalArgumentException("Time delta should be greater than zero");
    }

    this.timeDeltaMillis = timeDeltaMillis;
  }

  public void setMaxTimeMillis(long maxTimeMillis) {
    if (maxTimeMillis <= 0L) {
      throw new IllegalArgumentException("Max time should be greater than zero");
    }

    this.maxTimeMillis = maxTimeMillis;
  }

  public void setMultiplier(float multiplier) {
    if (multiplier <= 1.0f) {
      throw new IllegalArgumentException("Delay multiplier should be greater than 1.0");
    }

    this.multiplier = multiplier;
  }

  @Nonnull
  @Override
  public BackoffMark newMark() {
    return new Mark();
  }

  private final class Mark implements BackoffMark {
    float pow = 1.0f;

    @Override
    public long getNextRetryTime(@Nonnull TimeUnit timeUnit) {
      long time = (long) (startTimeMillis + pow * timeDeltaMillis);
      if (time <= 0L) {
        throw new IllegalStateException("Backoff strategy is not initialized properly");
      }
      pow = pow * multiplier;
      return timeUnit.convert(time > maxTimeMillis ? -1L : time, TimeUnit.MILLISECONDS);
    }
  }
}
