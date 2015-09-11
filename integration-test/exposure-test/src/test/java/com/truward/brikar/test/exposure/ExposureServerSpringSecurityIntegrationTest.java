package com.truward.brikar.test.exposure;

import com.truward.brikar.server.auth.SimpleServiceUser;
import org.junit.BeforeClass;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Integration tests for server that uses spring security.
 *
 * @author Alexander Shabanov
 */
public final class ExposureServerSpringSecurityIntegrationTest extends AbstractServerIntegrationTest {
  private static final SimpleServiceUser USER = new SimpleServiceUser("testonly", "test");

  @BeforeClass
  public static void initServer() {
    initServer(USER, LaunchMode.EXPOSURE_WITH_SPRING_SECURITY);
  }

  @Nullable
  @Override
  protected SimpleServiceUser getUser() {
    return USER;
  }
}
