package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.metric.Metrics;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.helpers.NOPLogger;

import javax.annotation.ParametersAreNonnullByDefault;

import static java.util.Objects.requireNonNull;

/**
 * Standard spring aspect that enables processing
 *
 * @author Alexander Shabanov
 */
@Aspect
@ParametersAreNonnullByDefault
public class StandardLapseLoggerAspect extends LapseLoggerAspectBase {
  private Logger logger = NOPLogger.NOP_LOGGER;

  public void setLogger(Logger logger) {
    this.logger = requireNonNull(logger);
  }

  @SuppressWarnings("unused")
  @Pointcut("execution(public * *(..))")
  public void publicMethod() {}

  @SuppressWarnings("unused")
  @Around("publicMethod() && @annotation(logLapse)")
  public Object around(ProceedingJoinPoint jp, LogLapse logLapse) throws Throwable {
    return invokeAndLog(jp, logLapse);
  }

  @Override
  protected void logMetrics(Metrics metrics) {
    LogUtil.propagateOrLogInfo(metrics, logger);
  }
}
