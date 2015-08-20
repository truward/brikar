package com.truward.brikar.server.controller.config;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * Controller, for reporting configuration.
 * <p>
 * This controller should be protected from external access and usually this should be used for debugging/development
 * purposes only.
 * </p>
 *
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/g/admin")
public class ConfigReportController {

  private Map<?, ?> appProperties;
  private boolean includeSystemProperties;

  public ConfigReportController() {
    setAppProperties(null);
    setIncludeSystemProperties(true);
  }

  public void setAppProperties(@Nullable Map<?, ?> appProperties) {
    this.appProperties = appProperties;
  }

  public void setIncludeSystemProperties(boolean includeSystemProperties) {
    this.includeSystemProperties = includeSystemProperties;
  }

  @RequestMapping(value = "/config", produces = MediaType.TEXT_PLAIN_VALUE)
  public void reportConfig(@Nonnull HttpServletResponse response) throws IOException {
    final PrintWriter writer = response.getWriter();

    writer.append("Generated at ").append(new Date().toString()).append('\n').append('\n');

    if (includeSystemProperties) {
      writeProperties(writer, "System", System.getProperties());
    }

    if (appProperties != null) {
      writeProperties(writer, "Application", appProperties);
    }
  }

  //
  // Private
  //

  private static void writeProperties(@Nonnull PrintWriter writer,
                                      @Nonnull String propertyBlockName,
                                      @Nonnull Map<?, ?> properties) {
    writer.append(propertyBlockName).append(' ').append("properties:\n");
    for (final Map.Entry<?, ?> entry : properties.entrySet()) {
      writer
          .append(Objects.toString(entry.getKey()))
          .append('=')
          .append(Objects.toString(entry.getValue().toString()))
          .append('\n');
    }
    writer.append('\n');
  }
}
