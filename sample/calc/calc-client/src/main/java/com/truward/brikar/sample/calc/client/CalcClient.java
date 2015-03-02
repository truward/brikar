package com.truward.brikar.sample.calc.client;

import com.truward.brikar.client.binder.support.StandardRestServiceBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.sample.calc.model.CalcModel;
import com.truward.brikar.sample.calc.model.CalcRestService;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * @author Alexander Shabanov
 */
public final class CalcClient {

  public static void main(String[] args) {
    final RestTemplate restTemplate = new RestTemplate();
    restTemplate.setMessageConverters(Arrays.<HttpMessageConverter<?>>asList(new ProtobufHttpMessageConverter()));

    // Creating a client so that all the methods will be usable against the particular endpoint
    final CalcRestService calcRestService = new StandardRestServiceBinder(restTemplate)
        .createClient("http://127.0.0.1:8080/rest/calc", CalcRestService.class);

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
