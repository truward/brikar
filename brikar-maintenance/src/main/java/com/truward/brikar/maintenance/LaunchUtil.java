package com.truward.brikar.maintenance;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Helper methods for integration tests.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class LaunchUtil {
  private LaunchUtil() {}

  /**
   * @return Port, available for local use
   */
  public static int getAvailablePort() {
    try {
      try (final ServerSocket serverSocket = new ServerSocket(0)) {
        return serverSocket.getLocalPort();
      }
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static URL[] getLocalClasspathUrls() {
    final URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
    return classLoader.getURLs();
  }

  public static String getJavaExecPath() {
    final String javaHome = System.getProperty("java.home");
    final StringBuilder result = new StringBuilder(javaHome.length() + 20);
    result.append(javaHome);

    final String sep = File.separator;
    if (!javaHome.endsWith(sep)) {
      result.append(sep);
    }

    result.append("bin").append(sep).append("java");
    return result.toString();
  }
}
