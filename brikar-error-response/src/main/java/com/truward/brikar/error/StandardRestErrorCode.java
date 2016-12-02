package com.truward.brikar.error;

/**
 * Represents standard rest error codes
 *
 * @author Alexander Shabanov
 */
public enum StandardRestErrorCode implements RestErrorCode {

  ACCESS_DENIED("AccessDenied"),

  INVALID_ARGUMENT("InvalidArgument"),

  UNSUPPORTED("Unsupported"),

  UNCATEGORIZED("Uncategorized");

  private final String codeName;

  public String getCodeName() {
    return codeName;
  }

  StandardRestErrorCode(String codeName) {
    this.codeName = codeName;
  }
}
