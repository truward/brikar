package com.truward.brikar.test.exposure.controller;

import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.StandardRestErrorCode;
import com.truward.brikar.error.model.ErrorV1;
import com.truward.brikar.server.controller.DefaultRestExceptionHandler;
import com.truward.brikar.test.exposure.ServiceErrors;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/api/test")
@ParametersAreNonnullByDefault
public final class ExposureRestController implements ExposureRestService, DefaultRestExceptionHandler {
  public static final String ACCESS_DENIED = "No access for non-admin";

  private ServiceErrors errors;

  public ExposureRestController(ServiceErrors errors) {
    this.errors = errors;
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ResponseBody
  public ErrorV1.ErrorResponse accessDeniedException(AccessDeniedException e) {
    return ErrorV1.ErrorResponse.newBuilder()
        .setError(errors.errorBuilder(StandardRestErrorCode.FORBIDDEN)
        .setMessage(ACCESS_DENIED))
        .build();
  }

  @Override
  public ExposureModel.HelloResponse greet(@RequestBody ExposureModel.HelloRequest request) {
    if (request.getPerson().isEmpty()) {
      return ExposureModel.HelloResponse.newBuilder().build(); // empty greeting
    }

    final String person = request.getPerson();

    if (person.equals("admin")) {
      throw new AccessDeniedException(ACCESS_DENIED);
    }

    if (person.equals("R2D2")) {
      throw new IllegalArgumentException("name");
    }

    if (person.equals("Darth Vader")) {
      throw new UnsupportedOperationException("name");
    }

    if (person.equals("Chewbacca")) {
      throw errors.teapot();
    }

    return ExposureModel.HelloResponse.newBuilder()
        .setGreeting("Hello, " + person)
        .build();
  }

  @Override
  public ExposureModel.HelloResponse getGreeting(@PathVariable("user") String user,
                                                 @PathVariable("type") String type,
                                                 @RequestParam("mode") String mode) {
    return ExposureModel.HelloResponse.newBuilder()
        .setGreeting("Hello, " + user + " of type " + type + " in mode " + mode)
        .build();
  }

  @Override
  public RestErrors getRestErrors() {
    return errors;
  }
}
