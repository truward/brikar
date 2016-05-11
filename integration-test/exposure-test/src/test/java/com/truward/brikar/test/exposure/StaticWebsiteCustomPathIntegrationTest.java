package com.truward.brikar.test.exposure;

import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests static content retrieval.
 *
 * @author Alexander Shabanov
 */
public final class StaticWebsiteCustomPathIntegrationTest extends ServerIntegrationTestBase {
  @BeforeClass
  public static void initServer() {
    initServer(null, LaunchMode.STATIC_WEBSITE_CUSTOM_PATH);
  }

  @Test
  public void shouldGetRobotsTxt() {
    withCustomRestBinder("/custom.txt", new TextRetrievalTestScenario() {
      @Override
      public void execute(@Nonnull RetrievalService retrievalService) {
        final String contents = retrievalService.getResource();
        assertNotNull(contents);
        assertTrue(contents.contains("custom"));
      }
    });
  }
}
