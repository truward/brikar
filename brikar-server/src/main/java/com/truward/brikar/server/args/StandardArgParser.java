package com.truward.brikar.server.args;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class StandardArgParser extends AbstractArgParser<StartArgs> {

  public StandardArgParser(@Nonnull String[] args) {
    super(args);
  }

  @Override
  protected StartArgs constructStartArgs() {
    return new StartArgs(port, configPath);
  }
}
