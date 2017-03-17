package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.error.StandardRestErrorCode;
import com.truward.brikar.error.model.ErrorModel;
import com.truward.brikar.error.parser.RestErrorParser;
import com.truward.brikar.maintenance.ServerApiUtil;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.protobuf.http.json.ProtobufJsonHttpMessageConverter;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.test.exposure.controller.ExposureRestController;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Basic integration tests.
 *
 * @author Alexander Shabanov
 */
public abstract class AbstractServerIntegrationTest extends ServerIntegrationTestBase {

  private final String normalGreetingText = "person";

  @Test
  public void shouldUseServicesUsingProtobufProtocol() throws IOException {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufHttpMessageConverter())) {
      final ExposureRestService exposureService = newClient(ExposureRestService.class, rof, "/api/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  @Test
  public void shouldUseServicesUsingJsonProtocol() throws IOException {
    // Set originating request ID for manual verification in logs
    MDC.put(LogUtil.REQUEST_VECTOR, "IntegTest-shouldUseServicesUsingJsonProtocol");
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufJsonHttpMessageConverter())) {
      final ExposureRestService exposureService = newClient(ExposureRestService.class, rof, "/api/test");

      checkNormalResponse(exposureService);
      checkErrors(exposureService);
    }
  }

  @Test
  public void shouldGetServerConfiguration() {
    withCustomRestBinder("/g/admin/config", retrievalService -> {
      final String config = retrievalService.getResource();
      assertNotNull(config);
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
      } catch (HttpStatusCodeException e) {
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

  private void checkErrors(@Nonnull ExposureRestService exposureService) throws IOException {
    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("R2D2").build());
      fail("Should not greet R2D2");
    } catch (HttpStatusCodeException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
      final ErrorModel.ErrorV2 error = RestErrorParser.parseError(e);
      assertEquals("Invalid Argument", error.getMessage());
      assertEquals(Collections.singletonList(ErrorModel.ErrorParameterV2.newBuilder().setKey("name").build()),
          error.getParametersList());
      assertEquals(StandardRestErrorCode.INVALID_ARGUMENT.getCodeName(), error.getCode());
    }

    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("Darth Vader").build());
      fail("Should not greet Darth Vader");
    } catch (HttpStatusCodeException e) {
      assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
      final ErrorModel.ErrorV2 error = RestErrorParser.parseError(e);
      assertEquals(StandardRestErrorCode.UNSUPPORTED.getCodeName(), error.getCode());
      assertEquals("name", error.getMessage());
    }

    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("Chewbacca").build());
      fail("Should not greet Chewbacca");
    } catch (HttpStatusCodeException e) {
      assertEquals(HttpStatus.I_AM_A_TEAPOT, e.getStatusCode());
      final ErrorModel.ErrorV2 error = RestErrorParser.parseError(e);
      assertEquals("TeapotIsNotAChewbacca", error.getCode());
      assertEquals(0, error.getParametersCount());
      assertEquals("I am a teapot", error.getMessage());
    }

    try {
      exposureService.greet(ExposureModel.HelloRequest.newBuilder().setPerson("admin").build());
      fail("Should not greet admin");
    } catch (HttpStatusCodeException e) {
      assertEquals(HttpStatus.UNAUTHORIZED, e.getStatusCode());
      final ErrorModel.ErrorV2 error = RestErrorParser.parseError(e);
      assertEquals(StandardRestErrorCode.ACCESS_DENIED.getCodeName(), error.getCode());
      assertEquals(ExposureRestController.ACCESS_DENIED, error.getMessage());
    }
  }
}
