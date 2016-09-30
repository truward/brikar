package com.truward.brikar.sample.calc.client;

import com.truward.brikar.client.rest.support.StandardRestClientBuilderFactory;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.calc.model.CalcModel;
import com.truward.brikar.sample.calc.model.CalcRestService;

import java.net.URI;

/**
 * @author Alexander Shabanov
 */
public final class CalcClient {

  public static void main(String[] args) {
    try (final StandardRestClientBuilderFactory restBinder = new StandardRestClientBuilderFactory(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      // Creating a client so that all the methods will be usable against the particular endpoint
      final CalcRestService calcRestService = restBinder.newClient(CalcRestService.class)
          .setUri(URI.create("http://127.0.0.1:8080/rest/calc"))
          .build();

      final CalcModel.GetVariables variables = calcRestService.getAllVariables();
      System.out.println("variables = " + variables);

      final CalcModel.Result result = calcRestService.execute(CalcModel.Operation.newBuilder()
          .setType(CalcModel.OperationType.PLUS)
          .addOperands(CalcModel.Operand.newBuilder().setValue(3).build())
          .addOperands(CalcModel.Operand.newBuilder().setValue(120).build())
          .addOperands(CalcModel.Operand.newBuilder().setVarName("one").build())
          .build());

      System.out.println("result = " + result.getValue());
    }
  }
}
