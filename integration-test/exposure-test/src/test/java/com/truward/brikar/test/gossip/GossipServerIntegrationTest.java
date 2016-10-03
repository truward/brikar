package com.truward.brikar.test.gossip;

import com.google.protobuf.StringValue;
import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import com.truward.brikar.maintenance.BrikarProcess;
import com.truward.brikar.maintenance.log.LogParser;
import com.truward.brikar.maintenance.log.message.LogMessage;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for request ID/originating request ID propagation.
 * Execution of test is not cheap, so everything is executed in one method.
 *
 * @author Alexander Shabanov
 */
public final class GossipServerIntegrationTest {

  private BrikarProcess gossipServer1;
  private List<BrikarProcess> gossipServers = new ArrayList<>();
  private RestOperationsFactory restOperationsFactory;
  private RestOperations restClient;

  @Before
  public void init() {
    // TODO: remove - use standard logger instead
    System.setProperty("logback.configurationFile", "default-service-logback.xml");

    gossipServer1 = BrikarProcess.newBuilder()
        .addTempLogger()
        .addTempConfiguration(new HashMap<String, String>() {
          {
            put("gossipService.gossipToken", "A");
          }
        })
        .setMainClass(GossipLauncher.class)
        .start();

    // wait until all servers launched
    gossipServer1.waitUntilLaunched(null);

    gossipServers.add(gossipServer1);

    restOperationsFactory = new RestOperationsFactory(new ProtobufHttpMessageConverter());
    restClient = restOperationsFactory.getRestOperations();
  }

  @After
  public void shutdown() {
    if (restOperationsFactory != null) {
      restOperationsFactory.close();
      restOperationsFactory = null;
      restClient = null;
    }

    for (final BrikarProcess process : gossipServers) {
      process.close();
    }
  }

  @Test
  public void shouldPropagateOriginatingId() throws IOException {
    final String startGossip = "GossipTime" + System.currentTimeMillis();

    // create custom headers with originating request ID
    final HttpHeaders headers = new HttpHeaders();
    final String originatingRequestId = "Gossiper" + ThreadLocalRandom.current().nextInt(100000);
    headers.set(TrackingHttpHeaderNames.ORIGINATING_REQUEST_ID, originatingRequestId);

    final HttpEntity<StringValue> gossipValue = restClient.exchange(
        URI.create(String.format("http://127.0.0.1:%d/rest/gossip?about=%s", gossipServer1.getPort(), startGossip)),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        StringValue.class
    );

    assertTrue(gossipValue.getHeaders().containsKey(TrackingHttpHeaderNames.REQUEST_ID));
    final String requestId = gossipValue.getHeaders().get(TrackingHttpHeaderNames.REQUEST_ID).get(0);
    assertTrue(StringUtils.hasLength(requestId));

    assertTrue(gossipValue.getBody().getValue().contains(startGossip));

    final String tempLog = gossipServer1.getTempLogBaseName();
    assertTrue(!tempLog.isEmpty());

    // parse log lines
    final File activeTempLog1 = gossipServer1.getActiveTempLog();
    assertNotNull("activeTempLog1 should not be null", activeTempLog1);
    final List<LogMessage> logLines = new LogParser().parse(activeTempLog1);

    boolean foundOriginatingRequestId = false;
    for (final LogMessage logMessage : logLines) {
      for (final String line : logMessage.getLines()) {
        if (line.contains(originatingRequestId)) {
          foundOriginatingRequestId = true;
        }
      }
    }

    assertTrue("Found orinating request ID", foundOriginatingRequestId);
  }
}
