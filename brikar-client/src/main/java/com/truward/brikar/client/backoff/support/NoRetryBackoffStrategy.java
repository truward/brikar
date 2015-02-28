package com.truward.brikar.client.backoff.support;

import com.truward.brikar.client.backoff.BackoffMark;
import com.truward.brikar.client.backoff.BackoffStrategy;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * Backoff strategy that does not allows to do any retries.
 *
 * @author Alexander Shabanov
 */
public final class NoRetryBackoffStrategy implements BackoffStrategy {
  private static final NoRetryBackoffStrategy INSTANCE = new NoRetryBackoffStrategy();

  private NoRetryBackoffStrategy() {}

  @Nonnull
  public static NoRetryBackoffStrategy getInstance() {
    return INSTANCE;
  }

  @Nonnull
  @Override
  public BackoffMark newMark() {
    return NEGATIVE_MARK;
  }

  //
  // Private
  //

  private static final NegativeMark NEGATIVE_MARK = new NegativeMark();

  private static final class NegativeMark implements BackoffMark {

    @Override
    public long getNextRetryTime(@Nonnull TimeUnit timeUnit) {
      return -1;
    }
  }
}
