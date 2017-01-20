package com.truward.brikar.common.log.metric;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface MetricsCollection {

  void add(Metrics metrics);

  /**
   * {@inheritDoc}
   * @return Produces metric string that can be used in log statement
   */
  String toString();
}
