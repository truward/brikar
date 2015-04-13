package com.truward.brikar.server.args;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public class StartArgs {
  private final int port;
  private final String configPath;

  public StartArgs(int port, @Nonnull String configPath) {
    this.port = port;
    this.configPath = configPath;
  }

  public int getPort() {
    return port;
  }

  @Nonnull
  public String getConfigPath() {
    return configPath;
  }
}
