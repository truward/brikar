package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.rpc.RpcService;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.annotation.Nonnull;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final RpcService rpcService = newClient(restBinder, RpcService.class, "/rest/RpcService");

      final ExposureModel.HelloResponse response = rpcService.sayHello(ExposureModel.HelloRequest.newBuilder()
          .setPerson("Alice").build());

      assertNotNull(response);
      assertEquals("Hello, Alice", response.getGreeting());
    }
  }

  @Test
  public void shouldGetExplorerPage() {
    withCustomRestBinder("/rest/RpcService/explorer.html", new TextRetrievalTestScenario() {
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
