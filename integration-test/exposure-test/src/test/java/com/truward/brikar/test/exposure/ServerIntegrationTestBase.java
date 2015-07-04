package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestBinder;
import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import org.eclipse.jetty.server.Server;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import javax.annotation.Nonnull;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Alexander Shabanov
 */
public abstract class ServerIntegrationTestBase {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private Thread thread;
  private int portNumber = 18080;
  private Server server;

  @Before
  public void init() {
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          ExposureServerLauncher.main(new String[]{
              "--port", Integer.toString(portNumber),
              "--graceful-shutdown-millis", "100"
          }, new ExposureServerLauncher.ServerAware() {
            @Override
            public void setServer(@Nonnull Server server) {
              ServerIntegrationTestBase.this.server = server;
            }
          });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    thread.start();
    log.info("Server started");
  }

  @After
  public void dispose() throws InterruptedException {
    if (server != null) {
      try {
        server.stop();
      } catch (Exception ignored) {
        // do nothing - failure to stop server might be caused by not being able to start it
      }
    }
    thread.join();
    log.info("Server stopped");
  }

  @Nonnull
  protected <T> T newClient(@Nonnull RestBinder binder, @Nonnull Class<T> clientClass, @Nonnull String relativePath) {
    return binder.newClient(clientClass)
        .setUsername("testonly").setPassword("test")
        .setUri(URI.create("http://127.0.0.1:" + portNumber + relativePath))
        .build();
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
