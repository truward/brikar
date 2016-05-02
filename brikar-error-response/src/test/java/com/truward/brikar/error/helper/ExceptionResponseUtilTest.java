package com.truward.brikar.error.helper;

import com.truward.brikar.error.model.ErrorModel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Alexander Shabanov
 */
@SuppressWarnings("ThrowableInstanceNeverThrown")
public final class ExceptionResponseUtilTest {

  @Test
  public void shouldShallowConvert() {
    // Given:
    final String message = "Something happened";
    final Throwable throwable = new RuntimeException(message);

    // When:
    final ErrorModel.Error e = ExceptionResponseUtil.shallowConvert(throwable);

    // Then:
    assertNotNull("message not missing", e.getMessage());
    assertEquals(message, e.getMessage());
    assertEquals("default code", 0, e.getCode());
    assertFalse("cause present", e.hasCause());
  }

  @Test
  public void shouldShallowConvertNullMessage() {
    // Given:
    final String message = null;
    final Throwable throwable = new RuntimeException(message);

    // When:
    final ErrorModel.Error e = ExceptionResponseUtil.shallowConvert(throwable);

    // Then:
    assertNotNull("message missing", e.getMessage());
    assertEquals(RuntimeException.class.getSimpleName(), e.getMessage());
    assertEquals("code present", 0, e.getCode());
    assertFalse("cause present", e.hasCause());
  }
}
