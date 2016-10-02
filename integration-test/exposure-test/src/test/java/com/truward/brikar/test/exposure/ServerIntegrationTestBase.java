package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.client.rest.ServiceClientCredentials;
import com.truward.brikar.client.rest.support.StandardRestServiceBinder;
import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.brikar.test.util.TestServerUtil;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * @author Alexander Shabanov
 */
public abstract class ServerIntegrationTestBase {
  private static final Logger LOG;

  static {
    // configure loggers - use default logger file
    System.setProperty("app.logback.rootLogId", "STDOUT");
    System.setProperty("app.logback.requestLogLevel", "TRACE");
    System.setProperty("logback.configurationFile", "default-service-logback.xml");
    LOG = LoggerFactory.getLogger(ServerIntegrationTestBase.class);
  }

  private static Thread THREAD;
  private static int PORT_NUMBER = TestServerUtil.getAvailablePort();
  private static Server SERVER;

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

  protected static void initServer(@Nullable SimpleServiceUser user, final LaunchMode launchMode) {
    THREAD = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final String props = StandardLauncher.CONFIG_KEY_PORT + "=" + PORT_NUMBER + "\n" +
              StandardLauncher.CONFIG_KEY_SHUTDOWN_DELAY + "=100\n" +
              "\n";

          final File tmpFile = File.createTempFile("brikarIntegrationTest", "properties");
          Files.write(Paths.get(tmpFile.toURI()), props.getBytes(StandardCharsets.UTF_8));

          ExposureServerLauncher.main(
              Collections.singletonList("file:" + tmpFile.getPath()),
              new ServerAware() {
                @Override
                public void setServer(@Nonnull Server server) {
                  SERVER = server;
                }
              },
              launchMode);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    THREAD.start();
    LOG.info("Server started");

    // protocol buffers message converter
    try (final RestOperationsFactory rof = new RestOperationsFactory(new StringHttpMessageConverter())) {
      final HealthCheckRestService healthCheckRestService = newClient(HealthCheckRestService.class,
          rof, user, getServerUrl("/rest"));
      waitUntilServerStarted(healthCheckRestService);
    }

    LOG.info("Server initialized");
  }

  @Nonnull
  protected static <T> T newClient(@Nonnull Class<T> serviceClass,
                                   @Nonnull RestOperationsFactory restOperationsFactory,
                                   @Nullable SimpleServiceUser user,
                                   @Nonnull URI uri) {
    final RestOperations restOperations = setupAndGetRestOperations(restOperationsFactory, user, uri);
    return new StandardRestServiceBinder(restOperations).createClient(uri, serviceClass);
  }

  protected <T> T newClient(@Nonnull Class<T> serviceClass,
                            @Nonnull RestOperationsFactory restOperationsFactory,
                            @Nonnull String relativePath) {
    return newClient(serviceClass, restOperationsFactory, getUser(), getServerUrl(relativePath));
  }

  @Nonnull
  private static RestOperations setupAndGetRestOperations(@Nonnull RestOperationsFactory factory,
                                                          @Nullable SimpleServiceUser user,
                                                          @Nonnull URI uri) {
    if (user != null) {
      factory.setCredentials(Collections.singletonList(new ServiceClientCredentials(uri,
          user.getUsername(), user.getPassword())));
    }

    return factory.getRestOperations();
  }

  @Nullable
  protected SimpleServiceUser getUser() {
    return null;
  }

  //protected

  @Nonnull
  protected static URI getServerUrl(String relPath) {
    return URI.create("http://127.0.0.1:" + PORT_NUMBER + relPath);
  }

  private static void waitUntilServerStarted(@Nonnull HealthCheckRestService healthCheckService) {
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

  protected void withCustomRestBinder(@Nonnull final String url, @Nonnull TextRetrievalTestScenario scenario) {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new StringHttpMessageConverter())) {
      final RestOperations restOperations = setupAndGetRestOperations(rof, getUser(),
          getServerUrl("/"));

      scenario.execute(new RetrievalService() {
        @Nonnull
        @Override
        public String getResource() {
          return restOperations.getForObject(getServerUrl(url), String.class);
        }
      });
    }
  }

  protected interface RetrievalService {
    @Nonnull String getResource();
  }

  protected interface TextRetrievalTestScenario {
    void execute(@Nonnull RetrievalService retrievalService);
  }
}
