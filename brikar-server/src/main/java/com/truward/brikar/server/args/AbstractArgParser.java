package com.truward.brikar.server.args;

import org.springframework.util.Assert;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public abstract class AbstractArgParser<A extends StartArgs> {
  public AbstractArgParser(@Nonnull String[] args) {
    Assert.notNull(args, "args");
    this.args = args;
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
  protected int pos = 0;
  protected boolean readyToStart;
  protected int port = StartArgs.DEFAULT_PORT;
  protected String configPath = StartArgs.DEFAULT_CONFIG_PATH;


  private void showHelp() {
    System.out.println("Usage:\n" +
        "--help,-h        Show help.\n" +
        "--port {NUMBER}  Port number, default value=" + StartArgs.DEFAULT_PORT + "\n" +
        "--config {PATH}  Path to config file, default value=" + StartArgs.DEFAULT_CONFIG_PATH + "\n" +
        "\n");
  }

  @Nonnull
  private String argValue(String valueName) {
    int nextPos = pos + 1;
    if (nextPos < args.length) {
      pos = nextPos;
      return args[pos];
    }
    throw new IllegalStateException("Extra argument expected for " + valueName);
  }

  private int doParse() {
    while (pos < args.length) {
      if ("--help".equals(args[pos]) || "-h".equals(args[pos])) {
        showHelp();
        return 0;
      }

      if (!parseCurrentArg()) {
        showHelp();
        return -1;
      }

      ++pos; // go to the next position
    }

    readyToStart = true;
    return 0;
  }

  protected boolean parseCurrentArg() {
    if ("--port".equals(args[pos])) {
      try {
        port = Integer.parseInt(argValue("port number"));
      } catch (NumberFormatException e) {
        throw new IllegalStateException("Unable to parse port number", e);
      }
    } else if ("--config".equals(args[pos])) {
      configPath = argValue("config location");
    }

    return true;
  }

  protected abstract A constructStartArgs();
}
