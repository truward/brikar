package com.truward.brikar.test.exposure;

import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.client.rest.ServiceClientCredentials;
import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.protobuf.http.json.ProtobufJsonHttpMessageConverter;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.test.exposure.model.ExposureModel;
import com.truward.brikar.test.exposure.service.ExposureRestService;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Integration tests for server that uses embedded jetty authentication means.
 *
 * @author Alexander Shabanov
 */
public final class ExposureServerIntegrationTest extends AbstractServerIntegrationTest {
  private static final SimpleServiceUser USER = new SimpleServiceUser("jettyAuthUser", "testExposureService");

  @BeforeClass
  public static void initServer() {
    initServer(USER, LaunchMode.EXPOSURE_WITH_SIMPLE_SECURITY);
  }

  @Nullable
  @Override
  protected SimpleServiceUser getUser() {
    return USER;
  }

  @Test
  public void shouldGetGreeting() {
    final URI baseUri = getServerUrl("/api/test");

    try (final RestOperationsFactory rof = new RestOperationsFactory(new ProtobufJsonHttpMessageConverter())) {
      rof.setCredentials(Collections.singletonList(new ServiceClientCredentials(baseUri, USER.getUsername(),
          USER.getPassword())));

      final RestOperations ro = rof.getRestOperations();

      final String requestId = TrackingHttpHeaderNames.getNestedRequestId("sample", 1);
      MDC.put(LogUtil.REQUEST_ID, requestId);
      final URI uri = UriComponentsBuilder.fromUri(baseUri)
          .pathSegment("exposure")
          .pathSegment("greet")
          .pathSegment("USER") // user path param
          .pathSegment("account")
          .pathSegment("SAMPLE") // type path param
          .queryParam("mode", "TEST") // mode query param
          .build().toUri();
      final ResponseEntity<ExposureModel.HelloResponse> responseEntity = ro.getForEntity(uri,
          ExposureModel.HelloResponse.class);

      final HttpHeaders headers = responseEntity.getHeaders();
      final List<String> rids = headers.get(TrackingHttpHeaderNames.REQUEST_ID);
      assertEquals(1, rids.size());

      final ExposureModel.HelloResponse resp = responseEntity.getBody();
      assertEquals("Hello, USER of type SAMPLE in mode TEST", resp.getGreeting());

      MDC.remove(LogUtil.REQUEST_ID);

      // verify with generated client wrapper
      final ExposureRestService exposureRestService = newClient(ExposureRestService.class, rof, "/api/test");
      final ExposureModel.HelloResponse resp2 = exposureRestService.getGreeting("USER", "SAMPLE", "TEST");
      assertEquals("Hello, USER of type SAMPLE in mode TEST", resp2.getGreeting());
    }
  }
}
