package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.protobuf.http.json.ProtobufJsonHttpMessageConverter;
import com.truward.brikar.test.exposure.controller.ExposureRestController;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import com.truward.brikar.common.healthcheck.HealthCheckRestService;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Alexander Shabanov
 */
public class ExposureServerIntegrationTest extends ServerIntegrationTestBase {

  @Test
  public void shouldRetrieveServerResponse() {

    // protocol buffers message converter
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter(), new StringHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      waitUntilServerStarted(newClient(restBinder, HealthCheckRestService.class, "/rest"));

      final ExposureRestService exposureService = newClient(restBinder, ExposureRestService.class, "/rest/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }

    // json message converter
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufJsonHttpMessageConverter(), new StringHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final ExposureRestService exposureService = newClient(restBinder, ExposureRestService.class, "/rest/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  private void checkNormalResponse(@Nonnull ExposureRestService exposureService) {
    final String text = "person";
    final ExposureModel.HelloResponse response = exposureService.greet(ExposureModel.HelloRequest.newBuilder()
        .setPerson(text).build());

    assertTrue(response.getGreeting().contains(text));
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
}
