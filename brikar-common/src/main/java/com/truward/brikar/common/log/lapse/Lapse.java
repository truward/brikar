package com.truward.brikar.common.log.lapse;

import javax.annotation.Nonnull;

/**
 * Identifies a single measurement of a service call, that needs to be logged.
 * Usually this is associated with the particular operation that takes certain amount of time and can fail.
 * The only purpose of this class is to provide structured data to logger writer which is capable to write lapses.
 *
 * @author Alexander Shabanov
 */
public interface Lapse {

  /**
   * @return Code, associated with a function call. Usually an interface name concatenated with method name
   */
  @Nonnull
  String getPlace();

  /**
   * @return Time in millisecond, which was needed to make a particular service call
   */
  long getTimeDeltaMillis();

  /**
   * @return True, if associated operation has been failed and false, if not
   */
  boolean isFailed();
}
