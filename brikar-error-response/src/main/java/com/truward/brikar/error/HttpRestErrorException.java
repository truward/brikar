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
  private final ErrorModel.ErrorV1 error;

  public HttpRestErrorException(int statusCode, ErrorModel.ErrorV1 error) {
    this.statusCode = statusCode;
    this.error = Objects.requireNonNull(error, "error");
  }

  public int getStatusCode() {
    return statusCode;
  }

  public ErrorModel.ErrorV1 getError() {
    return error;
  }
}
