package com.truward.brikar.server.test.launcher;

import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.brikar.server.test.auth.SimpleAuthenticatorUtilTest;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link StandardLauncher}.
 *
 * @author Alexander Shabanov
 */
public final class StandardLauncherTest {

  @Test
  public void shouldInitializeDefaults() throws IOException {
    final StandardLauncher launcher = new StandardLauncher(
        SimpleAuthenticatorUtilTest.createPropertySource("app=test"),
        "classpath:/");
    assertEquals("test", launcher.getPropertyResolver().getProperty("app"));
  }
}
