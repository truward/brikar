package com.truward.brikar.common.log;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method should be wrapped into an aspect that logs execution time for a method.
 *
 * @author Alexander Shabanov
 * @see LogUtil
 * @see com.truward.brikar.common.log.lapse.Lapse
 * @see com.truward.brikar.common.log.aspect.StandardLapseLoggerAspect
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogLapse {

  /**
   * Defines a name of an operation that appears in the logs.
   * Can be empty - in this case an aspect will fall back to the method signature name.
   *
   * @return Name
   */
  String value() default "";
}
