package com.truward.brikar.server.controller.config;

import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;

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
  private Map<?, ?> networkProperties;
  private boolean includeSystemProperties;
  private boolean includeNetworkProperties;

  public ConfigReportController() {
    setAppProperties(null);
    setIncludeSystemProperties(true);
    setIncludeNetworkProperties(true);
  }

  public void setAppProperties(@Nullable Map<?, ?> appProperties) {
    this.appProperties = appProperties;
  }

  public void setIncludeSystemProperties(boolean includeSystemProperties) {
    this.includeSystemProperties = includeSystemProperties;
  }

  public void setIncludeNetworkProperties(boolean includeNetworkProperties) {
    this.includeNetworkProperties = includeNetworkProperties;
  }

  @RequestMapping(value = "/config", produces = MediaType.TEXT_PLAIN_VALUE)
  public void reportConfig(@Nonnull HttpServletResponse response) throws IOException {
    final PrintWriter writer = response.getWriter();
    writer.append("Generated at ").append(new Date().toString()).append('\n').append('\n');
    appendProperties(writer);
  }

  //
  // Protected
  //

  protected void appendProperties(@Nonnull PrintWriter writer) throws IOException {
    if (includeSystemProperties) {
      writeProperties(writer, "System", System.getProperties());
    }

    if (appProperties != null) {
      writeProperties(writer, "Application", appProperties);
    }

    if (includeNetworkProperties) {
      if (networkProperties == null) {
        networkProperties = getNetworkProperties();
      }
      writeProperties(writer, "Network", networkProperties);
    }
  }

  protected static Map<String, String> getNetworkProperties() {
    final Map<String, String> sysInfoProperties = new HashMap<>();

    try {
      final Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
      final StringBuilder sb = new StringBuilder(100);

      while (networkInterfaces.hasMoreElements()) {
        final NetworkInterface networkInterface = networkInterfaces.nextElement();
        int i = 0;
        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
          sb.setLength(0);
          sb.append(networkInterface.getDisplayName()).append('.').append(i);
          sysInfoProperties.put(sb.toString(), interfaceAddress.getAddress().getHostAddress());
          ++i;
        }
      }

      InetAddress inetAddr = InetAddress.getLocalHost();
      sysInfoProperties.put("localhost.name", inetAddr.getHostName());
      sysInfoProperties.put("localhost.address", inetAddr.getHostAddress());
    } catch (UnknownHostException | SocketException e) {
      LoggerFactory.getLogger(ConfigReportController.class).error("Unable to pull network info", e);
    }

    return sysInfoProperties;
  }

  protected static void writeProperties(@Nonnull PrintWriter writer,
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
