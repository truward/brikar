package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogRegula;
import com.truward.time.TimeSource;
import com.truward.time.support.StandardTimeSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Standard spring aspect that enables processing
 *
 * @author Alexander Shabanov
 */
@Aspect
public class StandardRegulaLoggerAspect extends RegulaLoggerAspectBase implements InitializingBean {
  private Logger logger;
  private TimeSource timeSource;

  public StandardRegulaLoggerAspect() {
  }

  public StandardRegulaLoggerAspect(Logger logger, TimeSource timeSource) {
    this();
    setLogger(logger);
    setTimeSource(timeSource);
  }

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (logger == null) {
      setLogger(LoggerFactory.getLogger(StandardRegulaLoggerAspect.class));
    }

    if (timeSource == null) {
      timeSource = StandardTimeSource.INSTANCE;
    }
  }

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
