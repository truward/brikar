package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.error.model.ErrorModel;
import com.truward.brikar.maintenance.ServerApiUtil;
import com.truward.brikar.protobuf.http.ProtobufHttpConstants;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.protobuf.http.json.ProtobufJsonHttpMessageConverter;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.test.exposure.controller.ExposureRestController;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.apache.http.HttpHeaders;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

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
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufHttpMessageConverter())) {
      final ExposureRestService exposureService = newClient(ExposureRestService.class, rof, "/api/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  @Test
  public void shouldUseServicesUsingJsonProtocol() {
    // Set originating request ID for manual verification in logs
    MDC.put(LogUtil.REQUEST_ID, "IntegTest-shouldUseServicesUsingJsonProtocol");
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufJsonHttpMessageConverter())) {
      final ExposureRestService exposureService = newClient(ExposureRestService.class, rof, "/api/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  @Test
  public void shouldGetServerConfiguration() {
    withCustomRestBinder("/g/admin/config", new TextRetrievalTestScenario() {
      @Override
      public void execute(@Nonnull RetrievalService retrievalService) {
        final String config = retrievalService.getResource();
        assertNotNull(config);
      }
    });
  }

  @Test
  public void shouldGetAccessDenied() {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufHttpMessageConverter())) {
      final SimpleServiceUser user = getUser();
      assertNotNull(user);

      final ExposureRestService exposureService = ServerApiUtil.newClient(ExposureRestService.class, rof,
          new SimpleServiceUser(user.getUsername(), user.getPassword() + "1"), getServerUrl("/api/test"));

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

    final ExposureModel.HelloResponse emptyResponse = exposureService.greet(
        ExposureModel.HelloRequest.newBuilder().build());
    assertNotNull(emptyResponse);
    assertEquals("", emptyResponse.getGreeting());
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

    checkErrorModelInErrorResponse(exposureService);
  }

  private void checkErrorModelInErrorResponse(@Nonnull ExposureRestService exposureService) {
    final HttpServerErrorException exception;
    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("admin").build());
      fail("Should not greet admin");
      return;
    } catch (HttpServerErrorException e) {
      exception = e;
    }

    final List<String> contentTypeList = exception.getResponseHeaders().get(HttpHeaders.CONTENT_TYPE);
    assertEquals(1, contentTypeList.size());

    final MediaType actualContentType = MediaType.parseMediaType(contentTypeList.get(0));
    if (ProtobufHttpConstants.PROTOBUF_MEDIA_TYPE.isCompatibleWith(actualContentType)) {
      final ErrorModel.Error error;
      try (final ByteArrayInputStream bais = new ByteArrayInputStream(exception.getResponseBodyAsByteArray())) {
        error = ErrorModel.Error.parseDelimitedFrom(bais);
        assertEquals(ExposureRestController.ACCESS_DENIED, error.getMessage());
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    } else if (MediaType.APPLICATION_JSON.isCompatibleWith(actualContentType)) {
      assertEquals("{\"message\":\"" + ExposureRestController.ACCESS_DENIED + "\",\"parameters\":[]}",
          exception.getResponseBodyAsString());
    } else {
      fail("Unexpected content type: " + actualContentType);
    }
  }
}
