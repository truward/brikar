package com.truward.brikar.sample.calc.model;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
public interface CalcRestService {

  @RequestMapping("/variable")
  @ResponseBody
  CalcModel.GetVariables getAllVariables();

  @RequestMapping(value = "/execute", method = RequestMethod.POST)
  @ResponseBody
  CalcModel.Result execute(@RequestBody CalcModel.Operation operation);
}
