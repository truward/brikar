package com.truward.brikar.test.exposure.controller;

import com.truward.brikar.server.controller.AbstractRestController;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/test")
public final class ExposureRestController extends AbstractRestController implements ExposureRestService {

  @Override
  public ExposureModel.HelloResponse greet(@RequestBody ExposureModel.HelloRequest request) {
    final String person = request.getPerson();

    if (person.equals("R2D2")) {
      throw new IllegalArgumentException("Wrong person name");
    }

    return ExposureModel.HelloResponse.newBuilder()
        .setGreeting("Hello, " + person)
        .build();
  }
}
