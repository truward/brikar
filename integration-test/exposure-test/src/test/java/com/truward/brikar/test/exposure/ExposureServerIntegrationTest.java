package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Shabanov
 */
public class ExposureServerIntegrationTest {
  private Thread thread;
  private int portNumber = 8080;

  @Before
  public void init() {
    thread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          ExposureServerLauncher.main(new String[] {
              "--port", Integer.toString(portNumber)
          });
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    });

    thread.start();
  }

  @After
  public void dispose() throws InterruptedException {
    thread.join();
  }

  @Test
  public void shouldRetrieveServerResponse() throws Exception {
    Thread.sleep(5000L);

    try (final StandardRestBinder restBinder = new StandardRestBinder(new ProtobufHttpMessageConverter())) {
      restBinder.afterPropertiesSet();

      final ExposureRestService service = restBinder.newClient(ExposureRestService.class)
          .setUsername("testonly").setPassword("test")
          .setUri(URI.create("http://127.0.0.1:8080/rest/test"))
          .build();

      final String text = "person";
      final ExposureModel.HelloResponse response = service.greet(ExposureModel.HelloRequest.newBuilder()
          .setPerson(text).build());

      assertTrue(response.getGreeting().contains(text));
    }
  }
}
