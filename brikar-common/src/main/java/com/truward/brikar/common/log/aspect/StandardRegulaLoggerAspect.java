package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogRegula;
import com.truward.time.TimeSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.springframework.util.Assert;

/**
 * Standard spring aspect that enables processing
 *
 * @author Alexander Shabanov
 */
public class StandardRegulaLoggerAspect extends RegulaLoggerAspectBase {
  private final Logger logger;
  private final TimeSource timeSource;

  public StandardRegulaLoggerAspect(Logger logger, TimeSource timeSource) {
    Assert.notNull(logger, "logger");
    Assert.notNull(timeSource, "timeSource");
    this.logger = logger;
    this.timeSource = timeSource;
  }

//  public StandardRegulaLoggerAspect() {
//    this(LoggerFactory.getLogger(StandardRegulaLoggerAspect.class), StandardTimeSource.INSTANCE);
//  }

  @Pointcut("execution(public * *(..))")
  public void publicMethod() {}

  @Pointcut("@within(org.springframework.stereotype.Service)")
  public void withinService() {}

  @Around("publicMethod() && withinService() && @annotation(logRegula)")
  public Object around(ProceedingJoinPoint jp, LogRegula logRegula) throws Throwable {
    return around(logger, jp, logRegula);
  }

  @Override
  protected TimeSource getTimeSource() {
    return timeSource;
  }
}
