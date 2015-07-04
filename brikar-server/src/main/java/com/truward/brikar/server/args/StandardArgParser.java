package com.truward.brikar.server.args;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class StandardArgParser extends AbstractArgParser<StartArgs> {

  public StandardArgParser(@Nonnull String[] args, @Nonnull String defaultConfigPath) {
    super(args, defaultConfigPath);
  }

  @Override
  protected StartArgs constructStartArgs() {
    return new StartArgs(port, configPath, gracefulShutdownMillis);
  }
}
