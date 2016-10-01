package com.truward.brikar.client.test.rest;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.it.httpserver.producer.AbstractResponseProducer;
import com.truward.it.httpserver.support.DefaultItHttpServer;
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

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for {@link RestOperationsFactory}.
 *
 * @author Alexander Shabanov
 */
public class RestClientConnectionTest {
  private DefaultItHttpServer httpServer;
  private ThreadPoolTaskExecutor taskExecutor;

  private final String TEST_CONTENT = "test_content";

  @Before
  public void setUpServer() {
    taskExecutor = new ThreadPoolTaskExecutor();
    taskExecutor.setCorePoolSize(2);
    taskExecutor.setKeepAliveSeconds(1);
    taskExecutor.setBeanName("RestClientConnectionTest.taskExecutor");
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
    try (final RestOperationsFactory restOperationsFactory = new RestOperationsFactory(new StringHttpMessageConverter())) {
      final RestOperations restOperations = restOperationsFactory.getRestOperations();
      final String baseUrl = "http://127.0.0.1:" + httpServer.getPort();
      final String response = restOperations.getForObject(baseUrl, String.class);
      assertEquals(TEST_CONTENT, response);
    }
  }
}
