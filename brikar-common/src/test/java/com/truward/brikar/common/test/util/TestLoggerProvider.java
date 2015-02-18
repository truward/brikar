package com.truward.brikar.common.test.util;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.OutputStreamAppender;
import org.slf4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import java.io.ByteArrayOutputStream;

/**
 * A bean, that creates a logger which can be used for tests only (i.e. to verify that logs contain appropriately
 * formatted entries).
 *
 * @author Alexander Shabanov
 */
public final class TestLoggerProvider implements DisposableBean {
  final ByteArrayOutputStream byteArrayOutputStream;
  final OutputStreamAppender<ILoggingEvent> appender;
  final PatternLayoutEncoder encoder;
  final Logger log;

  public TestLoggerProvider() {
    this.byteArrayOutputStream = new ByteArrayOutputStream();

    // Get LoggerContext from SLF4J
    final LoggerContext context = new LoggerContext();

    // Encoder
    this.encoder = new PatternLayoutEncoder();
    encoder.setContext(context);
    encoder.setPattern("%d{ISO8601, UTC} [%thread] %-5level %X - %logger{36} - %msg%n");
    encoder.start();

    // OutputStreamAppender
    this.appender = new OutputStreamAppender<>();
    appender.setName("OutputStream Appender");
    appender.setContext(context);
    appender.setEncoder(this.encoder);
    appender.setOutputStream(this.byteArrayOutputStream);

    appender.start();

    ch.qos.logback.classic.Logger log = context.getLogger("test");
    log.addAppender(this.appender);

    this.log = log;
  }

  public void reset() {
    this.byteArrayOutputStream.reset();
  }

  public String getRawLogContents() {
    return byteArrayOutputStream.toString();
  }

  public Logger getLogger() {
    return this.log;
  }

  @Override
  public void destroy() throws Exception {
    appender.stop();
    encoder.stop();
  }
}
