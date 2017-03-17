package com.truward.brikar.server.controller;

import com.truward.brikar.error.HttpRestErrorException;
import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.model.ErrorModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.ParametersAreNonnullByDefault;

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

  @ExceptionHandler(HttpRestErrorException.class)
  @ResponseBody
  default ResponseEntity<ErrorModel.ErrorResponseV2> restErrorException(HttpRestErrorException e) {
    final ErrorModel.ErrorResponseV2 body = ErrorModel.ErrorResponseV2.newBuilder().setError(e.getError()).build();
    return new ResponseEntity<>(body, HttpStatus.valueOf(e.getStatusCode()));
  }

  @ExceptionHandler({IllegalArgumentException.class, UnsupportedOperationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  default ErrorModel.ErrorResponseV2 illegalArgumentOrUnsupportedException(Exception e) {
    return ErrorModel.ErrorResponseV2.newBuilder().setError(RestErrors.fromThrowable(e)).build();
  }

  @ExceptionHandler(Throwable.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  default ErrorModel.ErrorResponseV2 uncategorizedException(Throwable e) {
    return ErrorModel.ErrorResponseV2.newBuilder().setError(RestErrors.fromThrowable(e)).build();
  }
}
