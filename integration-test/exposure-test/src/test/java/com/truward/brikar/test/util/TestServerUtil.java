package com.truward.brikar.test.util;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Helper methods for integration tests.
 *
 * @author Alexander Shabanov
 */
public final class TestServerUtil {
  private TestServerUtil() {}

  /**
   * @return Port, available for local use
   */
  public static int getAvailablePort() {
    try {
      try (final ServerSocket serverSocket = new ServerSocket(0)) {
        return serverSocket.getLocalPort();
      }
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
