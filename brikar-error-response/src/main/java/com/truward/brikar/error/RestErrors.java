package com.truward.brikar.error;

import com.truward.brikar.error.model.ErrorModel;

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
  public static final String INVALID_ARGUMENT_MESSAGE = "Invalid Argument";
  public static final String UNCATEGORIZED_FAILURE_MESSAGE = "Uncategorized Failure";
  public static final String UNSUPPORTED_MESSAGE = "Unsupported Operation";

  private RestErrors() {
  } // Hidden

  public static ErrorModel.ErrorParameterV2 parameter(String name) {
    return ErrorModel.ErrorParameterV2.newBuilder().setKey(name).build();
  }

  public static String getMessageOrDefault(Throwable e, String defaultMessage) {
    final String message = e.getMessage();
    return message != null ? message : defaultMessage;
  }

  public static ErrorModel.ErrorV2 invalidArgument(String argumentName) {
    return invalidArgument(INVALID_ARGUMENT_MESSAGE, argumentName);
  }

  public static ErrorModel.ErrorV2 invalidArgument(String message, String argumentName) {
    return error(message, StandardRestErrorCode.INVALID_ARGUMENT,
        Collections.singletonList(parameter(argumentName)));
  }

  public static ErrorModel.ErrorV2 unsupported(String message, Collection<ErrorModel.ErrorParameterV2> parameters) {
    return error(message, StandardRestErrorCode.UNSUPPORTED, parameters);
  }

  public static ErrorModel.ErrorV2 fromThrowable(Throwable throwable) {
    if (throwable instanceof HttpRestErrorException) {
      final HttpRestErrorException e = (HttpRestErrorException) throwable;
      return e.getError();
    }

    String defaultMessage = UNCATEGORIZED_FAILURE_MESSAGE;
    RestErrorCode errorCode = StandardRestErrorCode.UNCATEGORIZED;

    if (throwable instanceof IllegalArgumentException) {
      return error(INVALID_ARGUMENT_MESSAGE, StandardRestErrorCode.INVALID_ARGUMENT,
          throwable.getMessage() != null ? Collections.singletonList(parameter(throwable.getMessage())) :
              Collections.emptyList());
    } else if (throwable instanceof UnsupportedOperationException) {
      defaultMessage = UNSUPPORTED_MESSAGE;
      errorCode = StandardRestErrorCode.UNSUPPORTED;
    }

    return error(throwable, defaultMessage, errorCode);
  }

  public static ErrorModel.ErrorV2 error(Throwable throwable,
                                         String defaultMessage,
                                         RestErrorCode code) {
    final String message = getMessageOrDefault(throwable, defaultMessage);
    return RestErrors.error(message, code, Collections.emptyList());
  }

  public static ErrorModel.ErrorV2 error(String message,
                                         RestErrorCode code) {
    return error(message, code, Collections.emptyList());
  }

  public static ErrorModel.ErrorV2 error(String message,
                                         RestErrorCode code,
                                         Collection<ErrorModel.ErrorParameterV2> parameters) {
    return error(message, code, parameters, Collections.emptyList());
  }

  public static ErrorModel.ErrorV2 error(String message,
                                         RestErrorCode code,
                                         Collection<ErrorModel.ErrorParameterV2> parameters,
                                         Collection<ErrorModel.ErrorV2> innerErrors) {
    final ErrorModel.ErrorV2.Builder errorBuilder = ErrorModel.ErrorV2.newBuilder()
        .setMessage(message).setCode(code.getCodeName());

    if (!parameters.isEmpty()) {
      errorBuilder.addAllParameters(parameters);
    }

    if (!innerErrors.isEmpty()) {
      errorBuilder.addAllInnerErrors(innerErrors);
    }

    return errorBuilder.build();
  }
}
