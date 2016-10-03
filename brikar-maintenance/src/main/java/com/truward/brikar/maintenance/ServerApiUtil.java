package com.truward.brikar.maintenance;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.client.rest.ServiceClientCredentials;
import com.truward.brikar.client.rest.support.StandardRestServiceBinder;
import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import com.truward.brikar.server.auth.SimpleServiceUser;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.net.URI;
import java.util.Collections;

/**
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class ServerApiUtil {
  private ServerApiUtil() {}

  @Nonnull
  public static RestOperations setupAndGetRestOperations(RestOperationsFactory factory,
                                                         @Nullable SimpleServiceUser user,
                                                         URI uri) {
    if (user != null) {
      factory.setCredentials(Collections.singletonList(new ServiceClientCredentials(uri,
          user.getUsername(), user.getPassword())));
    }

    return factory.getRestOperations();
  }

  @Nonnull
  public static <T> T newClient(Class<T> serviceClass,
                                RestOperationsFactory restOperationsFactory,
                                @Nullable SimpleServiceUser user,
                                URI uri) {
    final RestOperations restOperations = setupAndGetRestOperations(restOperationsFactory, user, uri);
    return new StandardRestServiceBinder(restOperations).createClient(uri, serviceClass);
  }

  public static void waitUntilStarted(@Nullable SimpleServiceUser user, URI serverBaseUri) {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new StringHttpMessageConverter())) {
      final HealthCheckRestService healthCheckRestService = newClient(HealthCheckRestService.class,
          rof, user, serverBaseUri);
      waitUntilServerStarted(healthCheckRestService);
    }
  }

  //
  // Private
  //

  private static void waitUntilServerStarted(HealthCheckRestService healthCheckService) {
    final int maxAttempts = 150;
    final int attemptDelayMillis = 100;

    for (int i = 0; i < maxAttempts; ++i) {
      try {
        Thread.sleep(attemptDelayMillis);
      } catch (InterruptedException e) {
        Thread.interrupted();
      }

      try {
        final String result = healthCheckService.checkHealth();
        if (!HealthCheckRestService.OK.equals(result)) {
          throw new IllegalStateException();
        }
        return;
      } catch (ResourceAccessException ignored) {
        // do nothing - server is starting
      }
    }

    throw new IllegalStateException("Server takes too long to initialize");
  }
}
