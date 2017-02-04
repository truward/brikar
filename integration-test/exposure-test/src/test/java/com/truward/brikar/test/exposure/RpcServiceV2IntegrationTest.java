package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.rpc.RpcService;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Alexander Shabanov
 */
public final class RpcServiceV2IntegrationTest extends ServerIntegrationTestBase {

  @BeforeClass
  public static void initServer() {
    initServer(null, LaunchMode.RPC_SERVICE_V2);
  }

  @Test
  public void shouldExecuteSayHello() {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufHttpMessageConverter())) {
      final RpcService rpcService = newClient(RpcService.class, rof, "/rpc/api/RpcService");

      final ExposureModel.HelloResponse response = rpcService.sayHello(ExposureModel.HelloRequest.newBuilder()
          .setPerson("Alice").build());

      assertNotNull(response);
      assertEquals("Hello, Alice", response.getGreeting());
    }
  }
}
