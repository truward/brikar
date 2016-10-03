package com.truward.brikar.maintenance.log.message;

/**
 * @author Alexander Shabanov
 */
public final class NullLogMessage extends LogMessage {
  public static final NullLogMessage INSTANCE = new NullLogMessage();

  private NullLogMessage() {}

  @Override
  public boolean isNull() {
    return true;
  }

  @Override
  public String toString() {
    return "NullLogMessage";
  }
}
