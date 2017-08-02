package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.metric.Metrics;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A class, that propagates logger and never logs messages directly.
 */
@ParametersAreNonnullByDefault
public class PropagateLapseLoggerAspectBean extends LapseLoggerAspectBase {

  public Object around(ProceedingJoinPoint jp, LogLapse logLapse) throws Throwable {
    return invokeAndLog(jp, logLapse);
  }

  @Override
  protected void logMetrics(Metrics metrics) {
    LogUtil.propagate(metrics);
  }
}
