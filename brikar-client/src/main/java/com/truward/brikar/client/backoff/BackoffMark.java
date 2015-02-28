package com.truward.brikar.client.backoff;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

/**
 * Represents backoff mark.
 *
 * @author Alexander Shabanov
 */
public interface BackoffMark {

  /**
   * Returns time that the requestor should wait before starting the next retry.
   * If mark returns negative or zero value requestor should discontinue making any further requests.
   *
   * @param timeUnit Time unit for result
   * @return Non-negative delay if retry is expected, negative value is returned when caller is expected to stop
   */
  long getNextRetryTime(@Nonnull TimeUnit timeUnit);
}
