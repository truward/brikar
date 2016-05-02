package com.truward.brikar.test.exposure;

import com.google.gson.Gson;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.util.JsonFormat;
import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.error.model.ErrorModel;
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
import java.util.List;
import java.util.Map;

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
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final SimpleServiceUser user = getUser();
      assertNotNull(user);

      final ExposureRestService exposureService = newClient(restBinder, ExposureRestService.class, "/rest/test",
          new SimpleServiceUser(user.getUsername(), user.getPassword() + "1"));

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
      try {
        error = ErrorModel.Error.parseFrom(exception.getResponseBodyAsByteArray());
        assertEquals(ExposureRestController.ACCESS_DENIED, error.getMessage());
      } catch (InvalidProtocolBufferException e) {
        throw new AssertionError(e);
      }
    } else if (MediaType.APPLICATION_JSON.isCompatibleWith(actualContentType)) {
      final JsonFormat.Parser parser = JsonFormat.parser();
      try {
        final ErrorModel.Error.Builder errorBuilder = ErrorModel.Error.newBuilder();
        parser.merge(exception.getResponseBodyAsString(), errorBuilder);
        final ErrorModel.Error error = errorBuilder.build();

        assertEquals(ExposureRestController.ACCESS_DENIED, error.getMessage());
      } catch (InvalidProtocolBufferException e) {
        throw new AssertionError(e);
      }
    } else {
      fail("Unexpected content type: " + actualContentType);
    }
  }
}
