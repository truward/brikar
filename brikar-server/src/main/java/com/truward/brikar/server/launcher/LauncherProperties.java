package com.truward.brikar.server.launcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author Alexander Shabanov
 */
public interface LauncherProperties {
  int DEFAULT_PORT = 8080;
  int DEFAULT_GRACEFUL_SHUTDOWN_MILLIS = 5000;

  int getPort();

  @Nullable
  String getConfigPath();

  int getGracefulShutdownMillis();

  @Nullable
  String getSimpleSecuritySettingsFilePath();
}
