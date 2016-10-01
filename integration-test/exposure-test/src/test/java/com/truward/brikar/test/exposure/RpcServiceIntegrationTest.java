package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.rpc.RpcService;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.*;

/**
 * @author Alexander Shabanov
 */
public final class RpcServiceIntegrationTest extends ServerIntegrationTestBase {

  @BeforeClass
  public static void initServer() {
    initServer(null, LaunchMode.RPC_SERVICE);
  }

  @Test
  public void shouldExecuteSayHello() {
    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufHttpMessageConverter())) {
      final RpcService rpcService = newClient(RpcService.class, rof, "/rest/rpc/RpcService");

      final ExposureModel.HelloResponse response = rpcService.sayHello(ExposureModel.HelloRequest.newBuilder()
          .setPerson("Alice").build());

      assertNotNull(response);
      assertEquals("Hello, Alice", response.getGreeting());
    }
  }

  @Test
  public void shouldGetExplorerPage() {
    withCustomRestBinder("/rest/explorer/RpcService", new TextRetrievalTestScenario() {
      @Override
      public void execute(@Nonnull RetrievalService retrievalService) {
        final String contents = retrievalService.getResource();
        assertNotNull(contents);
        assertTrue("should contain html doctype", contents.contains("DOCTYPE html"));
        assertTrue("should contain sayHello method", contents.contains("sayHello"));
      }
    });
  }
}
