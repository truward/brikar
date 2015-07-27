package com.truward.brikar.server.args;

import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public abstract class AbstractArgParser<A extends StartArgs> {

  /**
   * Default port the server should listen to.
   */
  public static final int DEFAULT_PORT = 8080;

  /**
   * Determines when we should stop receiving incoming connections.
   */
  public static final int DEFAULT_GRACEFUL_SHUTDOWN_MILLIS = 5000;

  public AbstractArgParser(@Nonnull String[] args, @Nonnull String defaultConfigPath) {
    Assert.notNull(args, "args");
    Assert.notNull(defaultConfigPath, "defaultConfigPath");

    this.args = args;
    this.defaultConfigPath = defaultConfigPath;
    this.configPath = defaultConfigPath;
  }

  public final int parse() {
    try {
      return doParse();
    } catch (IllegalStateException e) {
      System.err.println("Error: " + e + "\n");
      showHelp();
      return -1;
    }
  }

  public final boolean isReadyToStart() {
    return readyToStart;
  }

  @Nonnull
  public final A getStartArgs() {
    if (!isReadyToStart()) {
      throw new IllegalStateException("Unable to get start args: not ready to start");
    }

    return constructStartArgs();
  }

  //
  // Private
  //

  // state
  private final String[] args;
  private final String defaultConfigPath;

  // arguments
  protected boolean readyToStart;
  protected int port = DEFAULT_PORT;
  protected String configPath;
  protected int gracefulShutdownMillis = DEFAULT_GRACEFUL_SHUTDOWN_MILLIS;


  private void showHelp() {
    System.out.println("Usage:\n" +
        "--help,-h        Show help.\n" +
        "--port {NUMBER}  Port number, default value=" + DEFAULT_PORT + "\n" +
        "--config {PATH}  Path to config file, default value=" + defaultConfigPath + "\n" +
        "--graceful-shutdown-millis {NUMBER} Time in milliseconds,\n" +
        "                 the server should wait to serve active connections before stopping,\n" +
        "                 default value=" + DEFAULT_GRACEFUL_SHUTDOWN_MILLIS + "\n" +
        "\n");
  }

  @Nonnull
  protected String stringArgValue(int pos, @Nonnull String valueName) {
    int nextPos = pos + 1;
    if (nextPos < args.length) {
      pos = nextPos;
      return args[pos];
    }
    throw new IllegalStateException("Extra argument expected for " + valueName);
  }

  protected int intArgValue(int pos, @Nonnull String valueName) {
    final String arg = stringArgValue(pos, valueName);
    try {
      return Integer.parseInt(arg);
    } catch (NumberFormatException e) {
      throw new IllegalStateException("Unable to parse " + valueName, e);
    }
  }

  private int doParse() {
    // try find help switch (position doesn't matter, it overrides anything)
    for (final String arg : args) {
      if ("--help".equals(arg) || "-h".equals(arg)) {
        showHelp();
        return 0;
      }
    }

    // parse arguments
    for (int pos = 0; pos < args.length; ++pos) {
      if (!parseCurrentArg(pos)) {
        showHelp();
        return -1;
      }
    }

    readyToStart = true;
    return 0;
  }

  protected boolean parseCurrentArg(int pos) {
    if ("--port".equals(args[pos])) {
      port = intArgValue(pos, "port number");
    } else if ("--config".equals(args[pos])) {
      configPath = stringArgValue(pos, "config location");
    } else if ("--graceful-shutdown-millis".equals(args[pos])) {
      gracefulShutdownMillis = intArgValue(pos, "graceful shutdown millis");
    }

    return true;
  }

  protected abstract A constructStartArgs();
}
