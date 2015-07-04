package com.truward.brikar.server.controller;

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
 * The standard implementation returns string representation of the exception's message.
 * </p>
 *
 * @author Alexander Shabanov
 */
public abstract class AbstractRestController {

  @ExceptionHandler({IllegalArgumentException.class, UnsupportedOperationException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ResponseBody
  public Object onGenericClientError(@Nonnull Exception e) {
    return getResponseObjectFromException(e);
  }

  @ExceptionHandler({IllegalStateException.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public Object onIllegalStateError(@Nonnull Exception e) {
    return getResponseObjectFromException(e);
  }

  @ExceptionHandler({Throwable.class})
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ResponseBody
  public Object onUnknownError(@Nonnull Throwable e) {
    return getResponseObjectFromException(e); // TODO: do not disclose exception reason
  }

  protected Object getResponseObjectFromException(@Nonnull Throwable e) {
    return e.getMessage();
  }
}
