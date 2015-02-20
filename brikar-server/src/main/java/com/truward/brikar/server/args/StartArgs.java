package com.truward.brikar.server.args;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public class StartArgs {
  public static final int DEFAULT_PORT = 8080;
  public static final String DEFAULT_CONFIG_PATH = "classpath:settings/default.properties";

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
