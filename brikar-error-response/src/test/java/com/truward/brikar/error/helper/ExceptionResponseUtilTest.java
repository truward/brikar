package com.truward.brikar.error.helper;

import com.truward.brikar.error.model.ErrorModel;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    assertTrue("message missing", e.hasMessage());
    assertEquals(message, e.getMessage());
    assertFalse("code present", e.hasCode());
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
    assertTrue("message missing", e.hasMessage());
    assertEquals(RuntimeException.class.getSimpleName(), e.getMessage());
    assertFalse("code present", e.hasCode());
    assertFalse("cause present", e.hasCause());
  }
}
