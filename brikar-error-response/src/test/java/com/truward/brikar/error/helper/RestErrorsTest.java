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
    final ErrorModel.ErrorV2 e = RestErrors.invalidArgument(argumentName);

    // Then:
    assertErrorEquals(e, "Invalid Argument", StandardRestErrorCode.INVALID_ARGUMENT, argumentName);
  }

  @Test
  public void shouldConvertInvalidArgumentWithCustomMessage() {
    // Given:
    final String argumentName = "argumentName";
    final String message = "Error message";

    // When:
    final ErrorModel.ErrorV2 e = RestErrors.invalidArgument(message, argumentName);

    // Then:
    assertErrorEquals(e, message, StandardRestErrorCode.INVALID_ARGUMENT, argumentName);
  }

  @Test
  public void shouldConvertUnsupportedError() {
    // Given:
    final String message = "GET request is not supported";
    final String target = "/send/sms";

    // When:
    final ErrorModel.ErrorV2 e = RestErrors.unsupported(message,
        Collections.singletonList(RestErrors.parameter(target)));

    // Then:
    assertErrorEquals(e, message, StandardRestErrorCode.UNSUPPORTED, target);
  }

  @Test
  public void shouldConvertIllegalArgumentException() {
    final ErrorModel.ErrorV2 e = RestErrors.fromThrowable(new IllegalArgumentException());

    // Then:
    assertErrorEquals(e, "Invalid Argument", StandardRestErrorCode.INVALID_ARGUMENT, null);
  }

  @Test
  public void shouldConvertIllegalArgumentExceptionWithName() {
    // Given:
    final String name = "foo";

    // When:
    final ErrorModel.ErrorV2 e = RestErrors.fromThrowable(new IllegalArgumentException(name));

    // Then:
    assertErrorEquals(e, RestErrors.INVALID_ARGUMENT_MESSAGE, StandardRestErrorCode.INVALID_ARGUMENT, name);
  }

  @Test
  public void shouldConvertUnsupportedException() {
    final ErrorModel.ErrorV2 e = RestErrors.fromThrowable(new UnsupportedOperationException());

    // Then:
    assertErrorEquals(e, "Unsupported Operation", StandardRestErrorCode.UNSUPPORTED, null);
  }

  @Test
  public void shouldConvertUncategorizedException() {
    final ErrorModel.ErrorV2 e = RestErrors.fromThrowable(new RuntimeException());

    // Then:
    assertErrorEquals(e, "Uncategorized Failure", StandardRestErrorCode.UNCATEGORIZED, null);
  }

  @Test
  public void shouldConvertCustomRestError() {
    final String message = "Custom Client Error Message";
    final String code = "Custom Code";
    final ErrorModel.ErrorV2 err = ErrorModel.ErrorV2.newBuilder().setMessage(message).setCode(code).build();

    final ErrorModel.ErrorV2 e = RestErrors.fromThrowable(new HttpRestErrorException(500, err));

    assertEquals(err, e);
  }

  @Test
  public void shouldConvertUncategorizedError() {
    // Given:
    final String message = "Ouch! Something bad happened!";
    final RuntimeException ex = new RuntimeException(message);

    // When:
    final ErrorModel.ErrorV2 e = RestErrors.fromThrowable(ex);

    // Then:
    assertErrorEquals(e, message, StandardRestErrorCode.UNCATEGORIZED, null);
  }

  @Test
  public void shouldConvertAccessDeniedError() {
    // Given:
    final String message = "Access is denied for non-admin users";

    // When:
    final ErrorModel.ErrorV2 e = RestErrors.error(message, StandardRestErrorCode.ACCESS_DENIED);

    // Then:
    assertErrorEquals(e, message, StandardRestErrorCode.ACCESS_DENIED, null);
  }

  //
  // Private
  //

  private static void assertErrorEquals(ErrorModel.ErrorV2 error,
                                        String message,
                                        RestErrorCode code,
                                        @Nullable String target) {
    assertErrorEquals(error, message, code, target, Collections.emptyList());
  }

  private static void assertErrorEquals(ErrorModel.ErrorV2 err,
                                        String message,
                                        RestErrorCode code,
                                        @Nullable String target,
                                        Collection<ErrorModel.ErrorV2> innerErrors) {
    assertEquals(message, err.getMessage());
    assertEquals(code.getCodeName(), err.getCode());
    if (target != null) {
      assertEquals(Collections.singletonList(ErrorModel.ErrorParameterV2.newBuilder()
          .setKey(target)
          .build()), err.getParametersList());
    } else {
      assertEquals("error target should be missing", 0, err.getParametersCount());
    }

    if (!innerErrors.isEmpty()) {
      assertEquals(innerErrors, err.getInnerErrorsList());
    } else {
      assertEquals(0, err.getInnerErrorsCount());
    }
  }
}
