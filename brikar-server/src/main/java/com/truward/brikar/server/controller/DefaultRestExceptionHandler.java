package com.truward.brikar.server.controller;

import com.truward.brikar.error.HttpRestErrorException;
import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.StandardRestErrorCodes;
import com.truward.brikar.error.model.ErrorV1;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletResponse;

/**
 * Base class for handling exceptions. It is not required to inherit this class, but it might bring some benefits such
 * as base exception handling.
 * <p>
 * The service that uses this class must provide a message converter that understands how to serialize error response.
 * </p>
 * <p>
 * The standard implementation returns string representation of the exception's message.
 * </p>
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface DefaultRestExceptionHandler {

  static String getMessage(Throwable e, String defaultMessage) {
    final String message = e.getMessage();
    return message != null ? message : defaultMessage;
  }

  static String getMessage(Throwable e) {
    return getMessage(e, "");
  }

  RestErrors getRestErrors();

  @ExceptionHandler(HttpRestErrorException.class)
  @ResponseBody
  default ResponseEntity<ErrorV1.ErrorResponse> restErrorException(
      HttpRestErrorException e,
      HttpServletResponse response) {
    response.setStatus(e.getStatusCode());

    final ErrorV1.ErrorResponse body = ErrorV1.ErrorResponse.newBuilder().setError(e.getError()).build();
    return new ResponseEntity<>(body, HttpStatus.valueOf(e.getStatusCode()));
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  default ErrorV1.ErrorResponse illegalArgument(IllegalArgumentException e) {
    return RestErrors.errorResponse(getRestErrors().errorBuilder(StandardRestErrorCodes.INVALID_ARGUMENT)
        .setTarget(getMessage(e))
        .build());
  }

  @ExceptionHandler(UnsupportedOperationException.class)
  @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
  @ResponseBody
  default ErrorV1.ErrorResponse unsupported(UnsupportedOperationException e) {
    return RestErrors.errorResponse(getRestErrors().errorBuilder(StandardRestErrorCodes.NOT_IMPLEMENTED)
        .setMessage(getMessage(e, StandardRestErrorCodes.NOT_IMPLEMENTED.getDescription()))
        .build());
  }

  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  default ErrorV1.ErrorResponse internalServerError(Throwable e) {
    return RestErrors.errorResponse(getRestErrors().errorBuilder(StandardRestErrorCodes.INTERNAL)
        .setMessage(getMessage(e, StandardRestErrorCodes.INTERNAL.getDescription()))
        .build());
  }
}
