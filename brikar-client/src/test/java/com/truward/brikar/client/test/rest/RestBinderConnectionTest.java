package com.truward.brikar.client.test.rest;

import com.truward.brikar.client.binder.RestServiceBinder;
import com.truward.brikar.client.binder.RestServiceBinderFactory;
import com.truward.brikar.client.rest.support.StandardRestBinder;
import com.truward.it.httpserver.ItHttpServer;
import com.truward.it.httpserver.ItResponseProducer;
import com.truward.it.httpserver.producer.AbstractResponseProducer;
import com.truward.it.httpserver.support.DefaultItHttpServer;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.task.support.ExecutorServiceAdapter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

/**
 * Integration tests for {@link com.truward.brikar.client.rest.support.StandardRestBinder}.
 *
 * @author Alexander Shabanov
 */
public class RestBinderConnectionTest {
  private DefaultItHttpServer httpServer;
  private ThreadPoolTaskExecutor taskExecutor;

  private final String TEST_CONTENT = "test_content";

  @Before
  public void setUpServer() {
    taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(2);
    taskExecutor.setKeepAliveSeconds(1);
    taskExecutor.setBeanName("RestBinderConnectionTest.taskExecutor");
    taskExecutor.afterPropertiesSet();

    httpServer = new DefaultItHttpServer(new ExecutorServiceAdapter(taskExecutor));
    httpServer.setResponseProducer(new AbstractResponseProducer() {
      @Override
      public HttpResponse create(HttpRequest request) throws IOException {
        return createResponse(HttpStatus.SC_OK, ContentType.TEXT_PLAIN, TEST_CONTENT);
      }
    });
    httpServer.initialize();
  }

  @After
  public void shutdownServer() {
    httpServer.stopServerLoop();
    taskExecutor.destroy();
  }

  @Test
  public void shouldMakeRequest() {
    try (final StandardRestBinder restBinder = new StandardRestBinder(new StringHttpMessageConverter())) {
      restBinder.setRestServiceBinderFactory(new TestRestServiceBinderFactory());
      restBinder.afterPropertiesSet();

      final TestCallerService testCallerService = restBinder.newClient(TestCallerService.class)
          .setUri(URI.create("http://127.0.0.1:" + httpServer.getPort()))
          .build();

      assertEquals(TEST_CONTENT, testCallerService.getContent());
    }
  }

  //
  // Private
  //

  private static final class TestRestServiceBinderFactory implements RestServiceBinderFactory {

    @Nonnull
    @Override
    public RestServiceBinder create(@Nonnull final RestOperations restOperations) {
      return new RestServiceBinder() {
        @Nonnull
        @Override
        public <T> T createClient(@Nonnull final String serviceBaseUrl,
                                  @Nonnull Class<T> restServiceClass,
                                  @Nonnull Class<?>... extraClasses) {
          final TestCallerService callerService = new TestCallerService() {
            @Nonnull
            @Override
            public String getContent() {
              return restOperations.getForObject(serviceBaseUrl, String.class);
            }
          };

          return restServiceClass.cast(callerService);
        }
      };
    }
  }

  private interface TestCallerService {
    @Nonnull
    String getContent();
  }
}
