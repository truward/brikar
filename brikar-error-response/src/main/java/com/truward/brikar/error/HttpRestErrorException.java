package com.truward.brikar.error;

import com.truward.brikar.error.model.ErrorModel;

import java.util.Objects;

/**
 * Represents standard REST exception that translates directly into error model.
 *
 * @author Alexander Shabanov
 */
public final class HttpRestErrorException extends RuntimeException {
  private final int statusCode;
  private final ErrorModel.ErrorV2 error;

  public HttpRestErrorException(int statusCode, ErrorModel.ErrorV2 error) {
    this.statusCode = statusCode;
    this.error = Objects.requireNonNull(error, "error");
  }

  public int getStatusCode() {
    return statusCode;
  }

  public ErrorModel.ErrorV2 getError() {
    return error;
  }
}
