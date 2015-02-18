package com.truward.brikar.common.log.regula;

/**
 * Identifies a time measurement data, that needs to be logged. Usually this is associated with the particular
 * operation that takes certain amount of time and can fail.
 * <p>
 * Note on naming: Regula is a latin word that means 'Sample'.
 * The latin word is used here because 'Sample' is a hugely overloaded term and Regula is used here to explicitly
 * emphasize the meaning.
 * </p>
 * @author Alexander Shabanov
 */
public interface Regula {
  String getPlace();

  long getStartTimeMillis();

  long getTimeDeltaMillis();

  boolean isFailed();
}
