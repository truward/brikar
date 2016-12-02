package com.truward.brikar.error.helper;

import com.truward.brikar.error.HttpRestErrorException;
import com.truward.brikar.error.RestErrorCode;
import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.StandardRestErrorCode;
import com.truward.brikar.error.model.ErrorModel;
import org.junit.Test;

import javax.annotation.Nullable;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author Alexander Shabanov
 */
public final class RestErrorsTest {

  @Test
  public void shouldConvertInvalidArgumentWithNoMessage() {
    // Given:
    final String argumentName = "argumentName";

    // When:
    final ErrorModel.ErrorResponseV1 e = RestErrors.invalidArgument(argumentName);

    // Then:
    assertErrorMessageEquals(e, "Invalid Argument", StandardRestErrorCode.INVALID_ARGUMENT, argumentName);
  }

  @Test
  public void shouldConvertInvalidArgumentWithCustomMessage() {
    // Given:
    final String argumentName = "argumentName";
    final String message = "Error message";

    // When:
    final ErrorModel.ErrorResponseV1 e = RestErrors.invalidArgument(message, argumentName);

    // Then:
    assertErrorMessageEquals(e, message, StandardRestErrorCode.INVALID_ARGUMENT, argumentName);
  }

  @Test
  public void shouldConvertUnsupportedError() {
    // Given:
    final String message = "GET request is not supported";
    final String target = "/send/sms";

    // When:
    final ErrorModel.ErrorResponseV1 e = RestErrors.unsupported(message, target);

    // Then:
    assertErrorMessageEquals(e, message, StandardRestErrorCode.UNSUPPORTED, target);
  }

  @Test
  public void shouldConvertIllegalArgumentException() {
    final ErrorModel.ErrorResponseV1 e = RestErrors.fromThrowable(new IllegalArgumentException());

    // Then:
    assertErrorMessageEquals(e, "Invalid Argument", StandardRestErrorCode.INVALID_ARGUMENT, null);
  }

  @Test
  public void shouldConvertIllegalArgumentExceptionWithName() {
    // Given:
    final String name = "foo";

    // When:
    final ErrorModel.ErrorResponseV1 e = RestErrors.fromThrowable(new IllegalArgumentException(name));

    // Then:
    assertErrorMessageEquals(e, "Invalid Argument", StandardRestErrorCode.INVALID_ARGUMENT, name);
  }

  @Test
  public void shouldConvertUnsupportedException() {
    final ErrorModel.ErrorResponseV1 e = RestErrors.fromThrowable(new UnsupportedOperationException());

    // Then:
    assertErrorMessageEquals(e, "Unsupported Operation", StandardRestErrorCode.UNSUPPORTED, null);
  }

  @Test
  public void shouldConvertUncategorizedException() {
    final ErrorModel.ErrorResponseV1 e = RestErrors.fromThrowable(new RuntimeException());

    // Then:
    assertErrorMessageEquals(e, "Uncategorized Failure", StandardRestErrorCode.UNCATEGORIZED, null);
  }

  @Test
  public void shouldConvertCustomRestError() {
    final String message = "Custom Client Error Message";
    final String code = "Custom Code";
    final ErrorModel.ErrorV1 err = ErrorModel.ErrorV1.newBuilder().setMessage(message).setCode(code).build();

    final ErrorModel.ErrorResponseV1 e = RestErrors.fromThrowable(new HttpRestErrorException(500, err));

    assertEquals(err, e.getError());
  }

  @Test
  public void shouldConvertUncategorizedError() {
    // Given:
    final String message = "Ouch! Something bad happened!";
    final RuntimeException ex = new RuntimeException(message);

    // When:
    final ErrorModel.ErrorResponseV1 e = RestErrors.fromThrowable(ex);

    // Then:
    assertErrorMessageEquals(e, message, StandardRestErrorCode.UNCATEGORIZED, null);
  }

  @Test
  public void shouldConvertAccessDeniedError() {
    // Given:
    final String message = "Access is denied for non-admin users";

    // When:
    final ErrorModel.ErrorResponseV1 e = RestErrors.response(message, StandardRestErrorCode.ACCESS_DENIED);

    // Then:
    assertErrorMessageEquals(e, message, StandardRestErrorCode.ACCESS_DENIED, null);
  }

  //
  // Private
  //

  private static void assertErrorMessageEquals(ErrorModel.ErrorResponseV1 response,
                                               String message,
                                               RestErrorCode code,
                                               @Nullable String target) {
    assertErrorMessageEquals(response, message, code, target, Collections.emptyList(), null);
  }

  private static void assertErrorMessageEquals(ErrorModel.ErrorResponseV1 response,
                                               String message,
                                               RestErrorCode code,
                                               @Nullable String target,
                                               Collection<ErrorModel.ErrorV1> details,
                                               @Nullable ErrorModel.InnerErrorV1 innerError) {
    assertNotNull("response error is missing", response.hasError());
    final ErrorModel.ErrorV1 err = response.getError();

    assertEquals(message, err.getMessage());
    assertEquals(code.getCodeName(), err.getCode());
    if (target != null) {
      assertEquals(target, err.getTarget());
    } else {
      assertTrue("error target should be missing", err.getTarget().isEmpty());
    }

    if (!details.isEmpty()) {
      assertEquals(details, err.getDetailsList());
    } else {
      assertEquals(0, err.getDetailsCount());
    }

    if (innerError != null) {
      assertEquals(innerError, err.getInnerError());
    } else {
      assertFalse("inner error should be missing", err.hasInnerError());
    }
  }
}
