package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.maintenance.LaunchUtil;
import com.truward.brikar.maintenance.ServerApiUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.server.Server;
import org.junit.AfterClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

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
  private static int PORT_NUMBER = LaunchUtil.getAvailablePort();
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

    ServerApiUtil.waitUntilStarted(user, getServerUrl("/rest"));
    LOG.info("Server initialized");
  }

  protected <T> T newClient(@Nonnull Class<T> serviceClass,
                            @Nonnull RestOperationsFactory restOperationsFactory,
                            @Nonnull String relativePath) {
    return ServerApiUtil.newClient(serviceClass, restOperationsFactory, getUser(), getServerUrl(relativePath));
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

  protected void withCustomRestBinder(@Nonnull final String url, @Nonnull TextRetrievalTestScenario scenario) {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new StringHttpMessageConverter())) {
      final RestOperations restOperations = ServerApiUtil.setupAndGetRestOperations(rof, getUser(),
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
