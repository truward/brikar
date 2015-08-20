package com.truward.brikar.server.test.launcher;

import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.brikar.server.test.auth.SimpleAuthenticatorUtilTest;
import org.junit.Test;
import org.springframework.core.env.PropertySource;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link StandardLauncher}.
 *
 * @author Alexander Shabanov
 */
public final class StandardLauncherTest {

  @Test
  public void shouldInitializeDefaults() throws Exception {
    final StandardLauncher launcher = new StandardLauncher(
        new Callable<PropertySource<?>>() {
          @Override
          public PropertySource<?> call() throws Exception {
            return SimpleAuthenticatorUtilTest.createPropertySource("app=test");
          }
        },
        "classpath:/");
    assertEquals("test", launcher.getPropertyResolver().getProperty("app"));
  }
}
