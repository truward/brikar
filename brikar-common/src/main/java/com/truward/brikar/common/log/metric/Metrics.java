package com.truward.brikar.common.log.metric;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * An interface to the metric, that can be recorded in the log record.
 *
 * @author Alexander Shabanov
 */
public interface Metrics {

  void appendTo(@Nonnull Appendable appendable) throws IOException;

  //
  // appendValue helpers
  //

  static void appendValue(Appendable appendable, boolean next, String name, String value) throws IOException {
    if (next) {
      appendable.append(", ");
    }
    appendable.append(name).append("=").append(value);
  }

  static void appendValue(Appendable appendable, boolean next, String name, boolean value) throws IOException {
    appendValue(appendable, next, name, Boolean.toString(value));
  }

  static void appendValue(Appendable appendable, boolean next, String name, long value) throws IOException {
    appendValue(appendable, next, name, Long.toString(value));
  }

  static void appendValue(Appendable appendable, boolean next, String name, int value) throws IOException {
    appendValue(appendable, next, name, Integer.toString(value));
  }

  //
  // append(*)ValueOrSkip methods - appends value only if it meets certain condition
  //

  static void appendPositiveValueOrSkip(Appendable appendable, boolean next, String name, long value) throws IOException {
    if (value < 0) {
      return;
    }
    appendValue(appendable, next, name, value);
  }

  static void appendPositiveValueOrSkip(Appendable appendable, boolean next, String name, int value) throws IOException {
    if (value < 0) {
      return;
    }
    appendValue(appendable, next, name, value);
  }

  static void appendTrueValueOrSkip(Appendable appendable, boolean next, String name, boolean value) throws IOException {
    if (value) {
      appendValue(appendable, next, name, true);
    }
  }
}
