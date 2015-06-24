package com.truward.brikar.server.test.launcher;

import com.truward.brikar.server.launcher.StandardLauncher;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Alexander Shabanov
 */
public class StandardLauncherTest {

  @Test
  public void shouldInitializeDefaults() {
    final StandardLauncher launcher = new StandardLauncher();
    assertEquals("classpath:/default.properties", launcher.getConfigPath());
  }
}
