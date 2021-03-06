package com.truward.brikar.common.log.lapse;

import com.truward.brikar.common.log.metric.Metrics;

import javax.annotation.Nonnull;

/**
 * Identifies a single measurement of a service call, that needs to be logged.
 * Usually this is associated with the particular operation that takes certain amount of time and can fail.
 * The only purpose of this class is to provide structured data to logger writer which is capable to write lapses.
 * Normally the lapse should be used internally by an aspect, that
 * processes {@link com.truward.brikar.common.log.LogLapse} annotation.
 *
 * @author Alexander Shabanov
 */
public interface Lapse extends Metrics {

  /**
   * @return Code, associated with a function call. Usually an interface name concatenated with method name
   */
  @Nonnull
  String getOperation();

  /**
   * @return Operation start time, in millisecond, may be omitted in which case this value will be negative
   */
  long getStartTime();

  /**
   * @return Time in millisecond, which was needed to make a particular service call
   */
  long getEndTime();

  /**
   * @return True, if associated operation has been failed and false, if not
   */
  boolean isFailed();
}
