package com.truward.brikar.server.controller;

import com.truward.brikar.common.error.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nonnull;

/**
 * Base class for handling exceptions. It is not required to inherit this class, but it might bring some benefits such
 * as base exception handling.
 * <p>
 * The service that uses this class must provide a message converter that understands how to serialize error response.
 * </p>
 * <p>
 * See also {@link ErrorResponse}.
 * </p>
 *
 * @author Alexander Shabanov
 */
public abstract class AbstractRestController {

  @ExceptionHandler({IllegalArgumentException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public ErrorResponse onParameterValueError(@Nonnull Exception e) {
    return getResponseObjectFromException(e);
  }

  protected ErrorResponse getResponseObjectFromException(@Nonnull Throwable e) {
    return ErrorResponse.from(e.getMessage());
  }
}
