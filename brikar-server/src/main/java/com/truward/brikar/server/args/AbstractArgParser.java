package com.truward.brikar.server.args;

import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public abstract class AbstractArgParser<A extends StartArgs> {
  public static final int DEFAULT_PORT = 8080;

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

  private final String[] args;
  private final String defaultConfigPath;
  protected boolean readyToStart;
  protected int port = DEFAULT_PORT;
  protected String configPath;


  private void showHelp() {
    System.out.println("Usage:\n" +
        "--help,-h        Show help.\n" +
        "--port {NUMBER}  Port number, default value=" + DEFAULT_PORT + "\n" +
        "--config {PATH}  Path to config file, default value=" + defaultConfigPath + "\n" +
        "\n");
  }

  @Nonnull
  private String argValue(int pos, String valueName) {
    int nextPos = pos + 1;
    if (nextPos < args.length) {
      pos = nextPos;
      return args[pos];
    }
    throw new IllegalStateException("Extra argument expected for " + valueName);
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
      try {
        port = Integer.parseInt(argValue(pos, "port number"));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("Unable to parse port number", e);
      }
    } else if ("--config".equals(args[pos])) {
      configPath = argValue(pos, "config location");
    }

    return true;
  }

  protected abstract A constructStartArgs();
}
