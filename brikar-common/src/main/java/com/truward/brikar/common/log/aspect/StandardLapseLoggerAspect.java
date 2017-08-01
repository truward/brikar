package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogLapse;
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
public class StandardLapseLoggerAspect extends LapseLoggerAspectBase implements InitializingBean {
  private Logger logger;
  private TimeSource timeSource;

  public void setLogger(Logger logger) {
    this.logger = logger;
  }

  public void setTimeSource(TimeSource timeSource) {
    this.timeSource = timeSource;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (logger == null) {
      setLogger(LoggerFactory.getLogger(StandardLapseLoggerAspect.class));
    }

    if (timeSource == null) {
      setTimeSource(StandardTimeSource.INSTANCE);
    }
  }

  @Pointcut("execution(public * *(..))")
  public void publicMethod() {}

  @Pointcut("@within(org.springframework.stereotype.Service)")
  public void withinService() {}

  @Around("publicMethod() && withinService() && @annotation(logLapse)")
  public Object around(ProceedingJoinPoint jp, LogLapse logLapse) throws Throwable {
    return around(logger, jp, logLapse);
  }

  @Override
  protected TimeSource getTimeSource() {
    return timeSource;
  }
}
