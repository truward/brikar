package com.truward.brikar.test.exposure;

import com.truward.brikar.server.auth.SimpleServiceUser;
import org.junit.BeforeClass;

import javax.annotation.Nonnull;

/**
 * Integration tests for server that uses spring security.
 *
 * @author Alexander Shabanov
 */
public final class ExposureServerSpringSecurityIntegrationTest extends AbstractServerIntegrationTest {
  private static final SimpleServiceUser USER = new SimpleServiceUser("testonly", "test");

  @BeforeClass
  public static void initServer() {
    initServer(USER, true);
  }

  @Nonnull
  @Override
  protected SimpleServiceUser getUser() {
    return USER;
  }
}
