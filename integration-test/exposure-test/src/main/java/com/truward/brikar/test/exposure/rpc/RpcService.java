package com.truward.brikar.test.exposure.rpc;

import com.truward.brikar.test.exposure.model.ExposureModel;

/**
 * @author Alexander Shabanov
 */
public interface RpcService {
  ExposureModel.HelloResponse sayHello(ExposureModel.HelloRequest request);
}
