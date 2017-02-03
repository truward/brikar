package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.rpc.RpcService;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;

import java.util.Set;

import static org.junit.Assert.*;

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
      final RestOperations ro = rof.getRestOperations();
      final Set<HttpMethod> methods = ro.optionsForAllow(getServerUrl("/rpc/api"));
      assertNotNull(methods);

      final RpcService rpcService = newClient(RpcService.class, rof, "/api/rpc/RpcService");

      final ExposureModel.HelloResponse response = rpcService.sayHello(ExposureModel.HelloRequest.newBuilder()
          .setPerson("Alice").build());

      assertNotNull(response);
      assertEquals("Hello, Alice", response.getGreeting());
    }
  }
}
