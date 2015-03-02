package com.truward.brikar.sample.calc.server.controller;

import com.truward.brikar.sample.calc.model.CalcModel;
import com.truward.brikar.sample.calc.model.CalcRestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest/calc")
public final class CalcRestController implements CalcRestService {

  private final Map<String, Integer> variables = new HashMap<>();

  {
    variables.put("one", 1);
  }

  @Override
  public CalcModel.GetVariables getAllVariables() {
    final CalcModel.GetVariables.Builder builder = CalcModel.GetVariables.newBuilder();
    for (final Map.Entry<String, Integer> e : variables.entrySet()) {
      builder.addVars(CalcModel.VariableHolder.newBuilder()
          .setVarName(e.getKey()).setValue(e.getValue())
          .build());
    }
    return builder.build();
  }

  @Override
  public CalcModel.Result execute(@RequestBody CalcModel.Operation operation) {
    int result = 0;
    for (int i = 0; i < operation.getOperandsCount(); ++i) {
      final int val = getOperandValue(operation.getOperands(i));
      if (i == 0) {
        result = val;
      } else {
        switch (operation.getType()) {
          case PLUS:
            result += val;
            break;

          case MINUS:
            result -= val;
            break;

          default:
            throw new UnsupportedOperationException("Unsupported operation type=" + operation.getType());
        }
      }
    }
    return CalcModel.Result.newBuilder().setValue(result).build();
  }

  //
  // Private
  //

  private int getOperandValue(@Nonnull CalcModel.Operand operand) {
    if (operand.hasValue()) {
      return operand.getValue();
    }

    if (!operand.hasVarName()) {
      throw new IllegalArgumentException("Both value and varName can't be empty for operand");
    }

    final Integer v = variables.get(operand.getVarName());
    if (v == null) {
      throw new IllegalArgumentException("Unknown variable " + operand.getVarName());
    }

    return v;
  }
}
