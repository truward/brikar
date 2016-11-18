package com.truward.brikar.test.exposure.service;

import com.truward.brikar.test.exposure.model.ExposureModel;
import org.springframework.web.bind.annotation.*;

/**
 * @author Alexander Shabanov
 */
public interface ExposureRestService {

  @RequestMapping(value = "/exposure/greet", method = RequestMethod.POST)
  @ResponseBody
  ExposureModel.HelloResponse greet(@RequestBody ExposureModel.HelloRequest request);

  @RequestMapping("/exposure/greet/{user}/account/{type}")
  @ResponseBody
  ExposureModel.HelloResponse getGreeting(@PathVariable("user") String user,
                                          @PathVariable("type") String type,
                                          @RequestParam("mode") String mode);
}
