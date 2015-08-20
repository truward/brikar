package com.truward.brikar.test.exposure;

import com.truward.brikar.client.binder.RestServiceBinder;
import com.truward.brikar.client.binder.RestServiceBinderFactory;
import com.truward.brikar.client.rest.RestBinder;
import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.protobuf.http.json.ProtobufJsonHttpMessageConverter;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.test.exposure.controller.ExposureRestController;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

/**
 * Basic integration tests.
 *
 * @author Alexander Shabanov
 */
public abstract class AbstractServerIntegrationTest extends ServerIntegrationTestBase {

  private final String normalGreetingText = "person";

  @Test
  public void shouldUseServicesUsingProtobufProtocol() {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final ExposureRestService exposureService = newClient(restBinder, ExposureRestService.class, "/rest/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  @Test
  public void shouldUseServicesUsingJsonProtocol() {
    // Set originating request ID for manual verification in logs
    MDC.put(LogUtil.ORIGINATING_REQUEST_ID, "IntegTest-shouldUseServicesUsingJsonProtocol");
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufJsonHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final ExposureRestService exposureService = newClient(restBinder, ExposureRestService.class, "/rest/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  @Test
  public void shouldGetServerConfiguration() {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new StringHttpMessageConverter())) {
      class ConfigFetcherService {
        final RestOperations restOperations;

        public ConfigFetcherService(@Nonnull RestOperations restOperations) {
          this.restOperations = restOperations;
        }

        @Nonnull
        public String getConfig() {
          return restOperations.getForObject(getServerUrl("/g/admin/config"), String.class);
        }
      }

      restBinder.setRestServiceBinderFactory(new RestServiceBinderFactory() {
        @Nonnull
        @Override
        public RestServiceBinder create(@Nonnull final RestOperations restOperations) {
          return new RestServiceBinder() {
            @Nonnull
            @Override
            public <T> T createClient(@Nonnull String serviceBaseUrl, @Nonnull Class<T> restServiceClass, @Nonnull Class<?>... extraClasses) {
              assertEquals(ConfigFetcherService.class, restServiceClass);
              return restServiceClass.cast(new ConfigFetcherService(restOperations));
            }
          };
        }
      });
      restBinder.afterPropertiesSet();

      final ConfigFetcherService configFetcherService = newClient(restBinder, ConfigFetcherService.class, "/");

      final String config = configFetcherService.getConfig();

      assertNotNull(config);
    }
  }

  @Test
  public void shouldGetAccessDenied() {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final ExposureRestService exposureService = newClient(restBinder, ExposureRestService.class, "/rest/test",
          new SimpleServiceUser(getUser().getUsername(), getUser().getPassword() + "1"));

      try {
        exposureService.greet(ExposureModel.HelloRequest.newBuilder()
            .setPerson(normalGreetingText).build());
        fail("Should not be able to use rest resource without proper authentication");
      } catch (HttpClientErrorException e) {
        assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
      }
    }
  }

  //
  // Private
  //

  private void checkNormalResponse(@Nonnull ExposureRestService exposureService) {
    final ExposureModel.HelloResponse response = exposureService.greet(ExposureModel.HelloRequest.newBuilder()
        .setPerson(normalGreetingText).build());

    assertTrue(response.getGreeting().contains(normalGreetingText));
  }

  private void checkErrors(@Nonnull ExposureRestService exposureService) {
    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("R2D2").build());
      fail("Should not greet R2D2");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
      assertEquals(ExposureRestController.WRONG_NAME, e.getResponseBodyAsString());
    }

    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("Darth Vader").build());
      fail("Should not greet Darth Vader");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
      assertEquals(ExposureRestController.UNSUPPORTED_NAME, e.getResponseBodyAsString());
    }

    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("Chewbacca").build());
      fail("Should not greet Chewbacca");
    } catch (HttpServerErrorException e) {
      assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, e.getStatusCode());
      assertTrue(e.getResponseBodyAsString().isEmpty());
    }
  }

  //
  // Protected
  //

  @Nonnull
  protected <T> T newClient(@Nonnull RestBinder binder, @Nonnull Class<T> clientClass, @Nonnull String relPath) {
    return newClient(binder, clientClass, relPath, getUser());
  }

  @Nonnull
  protected abstract SimpleServiceUser getUser();
}