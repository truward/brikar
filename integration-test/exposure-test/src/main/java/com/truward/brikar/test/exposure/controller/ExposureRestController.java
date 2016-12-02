package com.truward.brikar.test.exposure.controller;

import com.truward.brikar.error.HttpRestErrorException;
import com.truward.brikar.error.RestErrors;
import com.truward.brikar.error.StandardRestErrorCode;
import com.truward.brikar.error.model.ErrorModel;
import com.truward.brikar.server.controller.DefaultRestExceptionHandler;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/api/test")
public final class ExposureRestController implements ExposureRestService, DefaultRestExceptionHandler {
  public static final String ACCESS_DENIED = "No access for non-admin";

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ResponseBody
  public ErrorModel.ErrorResponseV1 accessDeniedException(AccessDeniedException e) {
    return RestErrors.response(e, "Access Denied", StandardRestErrorCode.ACCESS_DENIED);
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
      throw new HttpRestErrorException(HttpStatus.I_AM_A_TEAPOT.value(), ErrorModel.ErrorV1.newBuilder()
          .setCode("TeapotIsNotAChewbacca")
          .setMessage("I am a teapot")
          .build());
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
}
