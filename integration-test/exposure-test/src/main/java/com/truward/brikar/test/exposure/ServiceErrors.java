package com.truward.brikar.test.exposure;

import com.truward.brikar.error.HttpRestErrorException;
import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.model.ErrorV1;
import org.springframework.http.HttpStatus;

/**
 * Rest errors registration.
 */
public final class ServiceErrors extends RestErrors {
  public static final String SOURCE = "test-app";

  public HttpRestErrorException teapot() {
    return new HttpRestErrorException(
        HttpStatus.I_AM_A_TEAPOT.value(),
        ErrorV1.Error.newBuilder()
            .setSource(getSource())
            .setCode("TeapotIsNotAChewbacca")
            .setMessage("I am a teapot")
            .build());
  }

  @Override
  protected String getSource() {
    return SOURCE;
  }
}
