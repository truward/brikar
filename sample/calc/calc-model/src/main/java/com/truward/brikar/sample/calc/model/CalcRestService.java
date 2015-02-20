package com.truward.brikar.sample.calc.model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Alexander Shabanov
 */
public interface CalcRestService {

  @RequestMapping("/variable")
  @ResponseBody
  CalcModel.GetVariables getAllVariables();
}
