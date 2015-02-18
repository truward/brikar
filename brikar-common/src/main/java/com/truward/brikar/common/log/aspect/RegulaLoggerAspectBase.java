package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogRegula;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.regula.SimpleRegula;
import com.truward.time.TimeSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;

/**
 * Base class for regula logging aspects.
 *
 * @author Alexander Shabanov
 */
public abstract class RegulaLoggerAspectBase {

  protected final Object around(Logger log, ProceedingJoinPoint jp, LogRegula logRegula) throws Throwable {
    final SimpleRegula regula = new SimpleRegula();

    regula.setStartTime(getTimeSource());

    String place = logRegula.value();
    if (!StringUtils.hasLength(place)) {
      // no text in annotation value - fallback to signature name
      place = getPlaceFromJoinPoint(jp);
    }
    regula.setPlace(place);

    try {
      final Object result = jp.proceed();

      regula.setEndTime(getTimeSource());
      LogUtil.writeRegula(log, regula);

      return result;
    } catch (Exception e) {

      // record end of call time and write regula
      regula.setEndTime(getTimeSource());
      regula.setFailed(true);
      LogUtil.writeRegula(log, regula);

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
