package com.truward.brikar.server.launcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Default implementation of {@link LauncherProperties}.
 *
 * @author Alexander Shabanov
 */
public class DefaultLauncherProperties implements LauncherProperties {
  public static final String SYS_PROP_NAME_PORT = "brikar.settings.port";
  public static final String SYS_PROP_NAME_GRACEFUL_SHUTDOWN_MILLIS = "brikar.settings.gracefulShutdownMillis";
  public static final String SYS_PROP_NAME_CONFIG_PATH = "brikar.settings.path";
  public static final String SYS_PROP_SIMPLE_SECURITY_SETTINGS_FILE_PATH =
      "brikar.settings.simpleSecuritySettingsFilePath";

  private int port = DEFAULT_PORT;
  private int gracefulShutdownMillis;
  private String configPath;
  private String simpleSecuritySettingsFilePath;

  @Override
  public int getPort() {
    return port;
  }

  @Nullable
  @Override
  public String getConfigPath() {
    return configPath;
  }

  @Override
  public int getGracefulShutdownMillis() {
    return gracefulShutdownMillis;
  }

  @Nullable
  @Override
  public String getSimpleSecuritySettingsFilePath() {
    return simpleSecuritySettingsFilePath;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public void setGracefulShutdownMillis(int gracefulShutdownMillis) {
    this.gracefulShutdownMillis = gracefulShutdownMillis;
  }

  public void setConfigPath(@Nullable String configPath) {
    this.configPath = configPath;
  }

  public void setSimpleSecuritySettingsFilePath(@Nullable String simpleSecuritySettingsFilePath) {
    this.simpleSecuritySettingsFilePath = simpleSecuritySettingsFilePath;
  }

  @Nonnull
  public static DefaultLauncherProperties createWithSystemProperties() {
    final DefaultLauncherProperties props = new DefaultLauncherProperties();

    props.setPort(Integer.parseInt(System.getProperty(SYS_PROP_NAME_PORT, Integer.toString(DEFAULT_PORT))));
    props.setGracefulShutdownMillis(Integer.parseInt(System.getProperty(SYS_PROP_NAME_GRACEFUL_SHUTDOWN_MILLIS,
        Integer.toString(DEFAULT_GRACEFUL_SHUTDOWN_MILLIS))));
    props.setConfigPath(System.getProperty(SYS_PROP_NAME_CONFIG_PATH));
    props.setSimpleSecuritySettingsFilePath(System.getProperty(SYS_PROP_SIMPLE_SECURITY_SETTINGS_FILE_PATH));

    return props;
  }
}
