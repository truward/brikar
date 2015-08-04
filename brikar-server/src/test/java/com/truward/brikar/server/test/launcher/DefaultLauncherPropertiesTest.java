package com.truward.brikar.server.test.launcher;

import com.truward.brikar.server.launcher.DefaultLauncherProperties;
import com.truward.brikar.server.launcher.LauncherProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link DefaultLauncherProperties}.
 *
 * @author Alexander Shabanov
 */
public final class DefaultLauncherPropertiesTest {

  @Test
  public void shouldGetDefaultProperties() {
    // Given/When:
    final DefaultLauncherProperties properties = DefaultLauncherProperties.createWithSystemProperties();

    // Then:
    assertNull(properties.getConfigPath());
    assertNull(properties.getSimpleSecuritySettingsFilePath());
    assertEquals(LauncherProperties.DEFAULT_PORT, properties.getPort());
    assertEquals(LauncherProperties.DEFAULT_GRACEFUL_SHUTDOWN_MILLIS, properties.getGracefulShutdownMillis());
  }
}
