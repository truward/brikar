package com.truward.brikar.client.test.backoff;

import com.truward.brikar.client.backoff.BackoffMark;
import com.truward.brikar.client.backoff.BackoffStrategy;
import com.truward.brikar.client.backoff.support.NoRetryBackoffStrategy;
import com.truward.brikar.client.backoff.support.TruncatedExponentialBackoffStrategy;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests various backoff strategies.
 *
 * @author Alexander Shabanov
 */
public final class BackoffStrategyTest {

  @Test
  public void shouldExposeTruncExponentialBackoffBehavior() {
    // Given:
    final BackoffStrategy strategy = new TruncatedExponentialBackoffStrategy();

    // When:
    final BackoffMark mark = strategy.newMark();

    // Then:
    assertEquals(50L, mark.getNextRetryTime(TimeUnit.MILLISECONDS));
    assertEquals(100L, mark.getNextRetryTime(TimeUnit.MILLISECONDS));
    assertEquals(200L, mark.getNextRetryTime(TimeUnit.MILLISECONDS));
    assertEquals(400L, mark.getNextRetryTime(TimeUnit.MILLISECONDS));
    assertEquals(800L, mark.getNextRetryTime(TimeUnit.MILLISECONDS));
    assertEquals(-1, mark.getNextRetryTime(TimeUnit.MILLISECONDS));
  }

  @Test
  public void shouldTruncExpBackoffMarkConvertTimeUnit() {
    // Given:
    final BackoffMark mark = new TruncatedExponentialBackoffStrategy().newMark();

    // When:
    final long microsec = mark.getNextRetryTime(TimeUnit.MICROSECONDS);

    // Then:
    assertEquals(50000L, microsec);
  }

  @Test
  public void shouldSeeNoRetriesForNoRetryStrategy() {
    assertTrue(NoRetryBackoffStrategy.getInstance().newMark().getNextRetryTime(TimeUnit.MILLISECONDS) < 0L);
  }
}
