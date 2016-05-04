package com.truward.brikar.test.exposure.rpc;

import com.truward.brikar.test.exposure.model.ExposureModel;

/**
 * @author Alexander Shabanov
 */
public final class HelloRpcService implements RpcService {

  @Override
  public ExposureModel.HelloResponse sayHello(ExposureModel.HelloRequest request) {
    final String person = request.getPerson();

    return ExposureModel.HelloResponse.newBuilder()
        .setGreeting("Hello, " + person)
        .build();
  }
}
