package com.truward.brikar.test.exposure.service;

import com.truward.brikar.test.exposure.model.ExposureModel;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
public interface ExposureRestService {

  @RequestMapping(value = "/exposure/greet", method = RequestMethod.POST)
  @ResponseBody
  ExposureModel.HelloResponse greet(@RequestBody ExposureModel.HelloRequest request);
}
