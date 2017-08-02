package com.truward.brikar.common.log.aspect;

import com.truward.brikar.common.log.LogLapse;
import com.truward.brikar.common.log.lapse.SimpleLapse;
import com.truward.brikar.common.log.metric.Metrics;
import com.truward.time.TimeSource;
import com.truward.time.support.StandardTimeSource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Method;

import static java.util.Objects.requireNonNull;

/**
 * Base class for lapse logging aspects.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public abstract class LapseLoggerAspectBase {

  private TimeSource timeSource = StandardTimeSource.INSTANCE;

  public final void setTimeSource(TimeSource timeSource) {
    this.timeSource = requireNonNull(timeSource);
  }

  public final TimeSource getTimeSource() {
    return timeSource;
  }

  protected final Object invokeAndLog(ProceedingJoinPoint jp, LogLapse logLapse) throws Throwable {
    final SimpleLapse lapse = new SimpleLapse();

    lapse.setStartTime(getTimeSource());

    String place = logLapse.value();
    if (!StringUtils.hasLength(place)) {
      // no text in annotation value - fallback to signature name
      place = getPlaceFromJoinPoint(jp);
    }
    lapse.setOperation(place);

    try {
      final Object result = jp.proceed();

      lapse.setEndTime(getTimeSource());
      lapse.setFailed(false);

      return result;
    } catch (Exception e) {
      // record end of call time and write lapse
      lapse.setEndTime(getTimeSource());
      lapse.setFailed(true);

      throw e;
    } finally {
      logMetrics(lapse);
    }
  }

  protected abstract void logMetrics(Metrics metrics);

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
