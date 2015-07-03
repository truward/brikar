package com.truward.brikar.common.error;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Represents generic error response object. Default converters should understand this message.
 *
 * @author Alexander Shabanov
 */
public class ErrorResponse {
  private final String message;
  private final ErrorResponse cause;

  private ErrorResponse(@Nonnull String message, @Nullable ErrorResponse cause) {
    this.message = Objects.requireNonNull(message, "message");
    this.cause = cause;
  }

  @Nonnull
  public static ErrorResponse from(@Nonnull String message) {
    return from(message, null);
  }

  @Nonnull
  public static ErrorResponse from(@Nonnull String message, @Nullable ErrorResponse cause) {
    return new ErrorResponse(message, cause);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ErrorResponse)) return false;

    ErrorResponse that = (ErrorResponse) o;

    return message.equals(that.message) && !(cause != null ? !cause.equals(that.cause) : that.cause != null);

  }

  @Override
  public int hashCode() {
    int result = message.hashCode();
    result = 31 * result + (cause != null ? cause.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        ", cause=" + cause +
        '}';
  }
}
