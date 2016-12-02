package com.truward.brikar.error;

import com.truward.brikar.error.model.ErrorModel;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;

/**
 * Utility class for converting exceptions to the meaningful responses.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class RestErrors {
  private RestErrors() {} // Hidden

  public static String getMessageOrDefault(Throwable e, String defaultMessage) {
    final String message = e.getMessage();
    return message != null ? message : defaultMessage;
  }

  public static ErrorModel.ErrorResponseV1 invalidArgument(String argumentName) {
    return invalidArgument("Invalid Argument", argumentName);
  }

  public static ErrorModel.ErrorResponseV1 invalidArgument(String message, String argumentName) {
    return response(message, StandardRestErrorCode.INVALID_ARGUMENT, argumentName);
  }

  public static ErrorModel.ErrorResponseV1 unsupported(String message, @Nullable String target) {
    return response(message, StandardRestErrorCode.UNSUPPORTED, target);
  }

  public static ErrorModel.ErrorResponseV1 fromThrowable(Throwable throwable) {
    if (throwable instanceof HttpRestErrorException) {
      final HttpRestErrorException e = (HttpRestErrorException) throwable;
      return ErrorModel.ErrorResponseV1.newBuilder().setError(e.getError()).build();
    }

    String defaultMessage = "Uncategorized Failure";
    RestErrorCode errorCode = StandardRestErrorCode.UNCATEGORIZED;

    if (throwable instanceof IllegalArgumentException) {
      defaultMessage = "Invalid Argument";
      errorCode = StandardRestErrorCode.INVALID_ARGUMENT;
    } else if (throwable instanceof UnsupportedOperationException) {
      defaultMessage = "Unsupported Operation";
      errorCode = StandardRestErrorCode.UNSUPPORTED;
    }

    return response(throwable, defaultMessage, errorCode);
  }

  public static ErrorModel.ErrorResponseV1 response(Throwable throwable,
                                                    String defaultMessage,
                                                    RestErrorCode code) {
    String message = getMessageOrDefault(throwable, defaultMessage);
    String target = null;
    if (!message.contains(" ")) {
      // message looks like an argument name
      target = message;
      message = defaultMessage;
    }

    return RestErrors.response(message, code, target);
  }

  public static ErrorModel.ErrorResponseV1 response(String message,
                                                    RestErrorCode code) {
    return response(message, code, null);
  }

  public static ErrorModel.ErrorResponseV1 response(String message,
                                                    RestErrorCode code,
                                                    @Nullable String target) {
    return response(message, code, target, Collections.emptyList());
  }

  public static ErrorModel.ErrorResponseV1 response(String message,
                                                    RestErrorCode code,
                                                    @Nullable String target,
                                                    Collection<ErrorModel.ErrorV1> details) {
    final ErrorModel.ErrorV1.Builder errorBuilder = ErrorModel.ErrorV1.newBuilder()
        .setMessage(message).setCode(code.getCodeName());

    if (target != null) {
      errorBuilder.setTarget(target);
    }

    if (!details.isEmpty()) {
      errorBuilder.addAllDetails(details);
    }

    return ErrorModel.ErrorResponseV1.newBuilder().setError(errorBuilder.build()).build();
  }
}
