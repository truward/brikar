package com.truward.brikar.sample.calc.server.controller;

import com.truward.brikar.sample.calc.model.CalcModel;
import com.truward.brikar.sample.calc.model.CalcRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/calc")
public final class CalcRestController implements CalcRestService {

  @Override
  public CalcModel.GetVariables getAllVariables() {
    return CalcModel.GetVariables.newBuilder()
        .addVars(CalcModel.VariableHolder.newBuilder()
            .setVarName("over")
            .setValue(9000)
            .build())
        .build();
  }
}
