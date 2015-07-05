package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestBinder;
import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.Nonnull;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Alexander Shabanov
 */
public abstract class ServerIntegrationTestBase {
  private static final Logger LOG = LoggerFactory.getLogger(ServerIntegrationTestBase.class);

  private static Thread THREAD;
  private static int PORT_NUMBER = 18080;
  private static Server SERVER;

  @BeforeClass
  public static void initServer() {
    THREAD = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          ExposureServerLauncher.main(new String[]{
              "--port", Integer.toString(PORT_NUMBER),
              "--graceful-shutdown-millis", "100"
          }, new ExposureServerLauncher.ServerAware() {
            @Override
            public void setServer(@Nonnull Server server) {
              SERVER = server;
            }
          });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    THREAD.start();
    LOG.info("Server started");

    // protocol buffers message converter
    try (final StandardRestBinder restBinder = new StandardRestBinder(new StringHttpMessageConverter())) {
      restBinder.afterPropertiesSet();
      waitUntilServerStarted(newClient(restBinder, HealthCheckRestService.class, "/rest"));
    }
    LOG.info("Server initialized");
  }

  @AfterClass
  public static void stopServer() {
    if (SERVER != null) {
      try {
        SERVER.stop();
      } catch (Exception ignored) {
        // do nothing - failure to stop server might be caused by not being able to start it
      }

      SERVER = null;
    }

    try {
      THREAD.join();
    } catch (InterruptedException e) {
      Thread.interrupted();
    }

    LOG.info("Server stopped");
  }

  @Before
  public void init() {

  }

  @After
  public void dispose() throws InterruptedException {

  }

  @Nonnull
  protected static <T> T newClient(@Nonnull RestBinder binder, @Nonnull Class<T> clientClass, @Nonnull String relPath) {
    return binder.newClient(clientClass)
        .setUsername("testonly").setPassword("test")
        .setUri(getServerUrl(relPath))
        .build();
  }

  @Nonnull
  protected static URI getServerUrl(String relPath) {
    return URI.create("http://127.0.0.1:" + PORT_NUMBER + relPath);
  }

  protected static void waitUntilServerStarted(@Nonnull HealthCheckRestService healthCheckService) {
    for (int i = 0; i < 100; ++i) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }

      try {
        final String result = healthCheckService.checkHealth();
        assertEquals(HealthCheckRestService.OK, result);
        return;
      } catch (ResourceAccessException ignored) {
        // do nothing - server is starting
      }
    }

    fail("Server initialization failed");
  }
}
