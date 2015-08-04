package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.time.TimeSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Base class for lapse logging aspects.
 *
 * @author Alexander Shabanov
 */
public abstract class LapseLoggerAspectBase {

  protected final Object around(Logger log, ProceedingJoinPoint jp, LogLapse logLapse) throws Throwable {
    final SimpleLapse regula = new SimpleLapse();

    regula.setStartTime(getTimeSource());

    String place = logLapse.value();
    if (!StringUtils.hasLength(place)) {
      // no text in annotation value - fallback to signature name
      place = getPlaceFromJoinPoint(jp);
    }
    regula.setOperation(place);

    try {
      final Object result = jp.proceed();

      regula.setEndTime(getTimeSource());
      LogUtil.writeLapse(log, regula);

      return result;
    } catch (Exception e) {

      // record end of call time and write lapse
      regula.setEndTime(getTimeSource());
      regula.setFailed(true);
      LogUtil.writeLapse(log, regula);

      throw e;
    }
  }

  protected abstract TimeSource getTimeSource();

  //
  // Private
  //

  private static String getPlaceFromJoinPoint(ProceedingJoinPoint jp) {
    final Signature signature = jp.getSignature();
    if (signature instanceof MethodSignature) {
      final Method method = ((MethodSignature) signature).getMethod();
      return method.getDeclaringClass().getSimpleName() + '.' + method.getName();
    }

    // fallback to generic name
    return jp.getSignature().getName();
  }
}
