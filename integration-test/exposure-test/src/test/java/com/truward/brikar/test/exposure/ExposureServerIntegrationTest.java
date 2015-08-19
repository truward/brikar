package com.truward.brikar.test.exposure;

import com.truward.brikar.server.auth.SimpleServiceUser;
import org.junit.BeforeClass;

import javax.annotation.Nonnull;

/**
 * Integration tests for server that uses embedded jetty authentication means.
 *
 * @author Alexander Shabanov
 */
public final class ExposureServerIntegrationTest extends AbstractServerIntegrationTest {
  private static final SimpleServiceUser USER = new SimpleServiceUser("jettyAuthUser", "testExposureService");

  @BeforeClass
  public static void initServer() {
    initServer(USER, false);
  }

  @Nonnull
  @Override
  protected SimpleServiceUser getUser() {
    return USER;
  }
}
