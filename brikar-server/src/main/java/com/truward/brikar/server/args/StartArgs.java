package com.truward.brikar.server.args;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public class StartArgs {
  private final int port;
  private final String configPath;
  private final int gracefulShutdownMillis;

  public StartArgs(int port, @Nonnull String configPath, int gracefulShutdownMillis) {
    if (port <= 0) {
      throw new IllegalArgumentException("Invalid port value=" + port);
    }

    if (gracefulShutdownMillis < 0) {
      throw new IllegalArgumentException("Invalid gracefulShutdownMillis value=" + gracefulShutdownMillis);
    }

    this.port = port;
    this.configPath = configPath;
    this.gracefulShutdownMillis = gracefulShutdownMillis;
  }

  public int getPort() {
    return port;
  }

  @Nonnull
  public String getConfigPath() {
    return configPath;
  }

  public int getGracefulShutdownMillis() {
    return gracefulShutdownMillis;
  }
}
