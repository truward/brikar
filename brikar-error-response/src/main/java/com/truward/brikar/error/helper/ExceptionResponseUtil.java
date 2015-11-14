package com.truward.brikar.error.helper;

import com.truward.brikar.error.model.ErrorModel;

import javax.annotation.Nonnull;

/**
 * Utility class for converting exceptions to the meaningful responses.
 *
 * @author Alexander Shabanov
 */
public final class ExceptionResponseUtil {
  private ExceptionResponseUtil() {} // Hidden

  @Nonnull
  public static ErrorModel.Error shallowConvert(@Nonnull Throwable throwable) {
    final String message = throwable.getMessage() != null ? throwable.getMessage() :
        throwable.getClass().getSimpleName();

    return ErrorModel.Error.newBuilder()
        .setMessage(message)
        .build();
  }
}
