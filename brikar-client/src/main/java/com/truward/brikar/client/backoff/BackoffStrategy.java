package com.truward.brikar.client.backoff;

import javax.annotation.Nonnull;

/**
 * Represents an abstraction for backoff strategy (e.g. for retries).
 * See also <a href="http://en.wikipedia.org/wiki/Exponential_backoff">Exponential Backoff</a>.
 *
 * @author Alexander Shabanov
 */
public interface BackoffStrategy {

  /**
   * Creates a new mark for certain process that needs to be retried in case of an error.
   * Returned instances might not be thread safe. It is assumed that retry behavior will be done in one thread in a
   * blocking manner.
   *
   * @return Marker instance that holds current retry delay state
   */
  @Nonnull
  BackoffMark newMark();
}
