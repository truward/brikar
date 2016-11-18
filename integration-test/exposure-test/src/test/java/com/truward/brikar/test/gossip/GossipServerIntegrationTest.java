package com.truward.brikar.test.gossip;

import com.google.protobuf.StringValue;
import com.truward.brikar.client.rest.RestOperationsFactory;
import com.truward.brikar.common.tracking.TrackingHttpHeaderNames;
import com.truward.brikar.maintenance.BrikarProcess;
import com.truward.brikar.maintenance.log.LogParser;
import com.truward.brikar.maintenance.log.message.LogMessage;
import com.truward.brikar.protobuf.http.ProtobufHttpMessageConverter;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestOperations;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.Assert.*;

/**
 * Integration tests for request ID/originating request ID propagation.
 * Execution of test is not cheap, so everything is executed in one method.
 *
 * @author Alexander Shabanov
 */
public final class GossipServerIntegrationTest {

  // Gossip chain: gossipServer1 calls gossipServer2
  private BrikarProcess gossipServer1;
  private BrikarProcess gossipServer2;

  private List<BrikarProcess> gossipServers = new ArrayList<>();
  private RestOperationsFactory restOperationsFactory;
  private RestOperations restClient;

  @Before
  public void init() {
    // Initialize default logger
    StandardLauncher.ensureLoggersConfigured();

    // prepare end of the gossip chain
    gossipServer2 = BrikarProcess.newBuilder()
        .addTempLogger()
        .addTempConfiguration(new HashMap<String, String>() {
          {
            put("gossipService.gossipToken", "B");
          }
        })
        .setMainClass(GossipLauncher.class)
        .start();

    // prepare start of the gossip chain
    gossipServer1 = BrikarProcess.newBuilder()
        .addTempLogger()
        .addTempConfiguration(new HashMap<String, String>() {
          {
            put("gossipService.gossipToken", "A");

            put("gossipService.gossipChainMode", "NEXT");

            // tells gossip server 1 to talk with gossip server 2
            put("gossipService.remote.gossipService.uri",
                String.format("http://127.0.0.1:%d/api/gossip", gossipServer2.getPort()));
          }
        })
        .setMainClass(GossipLauncher.class)
        .start();


    // wait until all servers launched
    gossipServer1.waitUntilLaunched(null);
    gossipServer2.waitUntilLaunched(null);

    gossipServers.add(gossipServer1);
    gossipServers.add(gossipServer2);

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
    MDC.remove(TrackingHttpHeaderNames.REQUEST_ID);

    final String startGossip = "GossipTime" + System.currentTimeMillis();

    // create custom headers with originating request ID
    final HttpHeaders headers = new HttpHeaders();
    final String originatingRequestId = "Gossiper" + ThreadLocalRandom.current().nextInt(100000);
    headers.set(TrackingHttpHeaderNames.REQUEST_ID, originatingRequestId);

    final HttpEntity<StringValue> gossipValue = restClient.exchange(
        URI.create(String.format("http://127.0.0.1:%d/api/gossip?about=%s", gossipServer1.getPort(), startGossip)),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        StringValue.class
    );

    assertTrue(gossipValue.getHeaders().containsKey(TrackingHttpHeaderNames.REQUEST_ID));
    final String requestId = gossipValue.getHeaders().get(TrackingHttpHeaderNames.REQUEST_ID).get(0);
    assertTrue(StringUtils.hasLength(requestId));

    final String gossip = gossipValue.getBody().getValue();
    assertEquals("Gossip is malformed or missing required gossip tokens", "B-A-" + startGossip, gossip);

    final String tempLog = gossipServer1.getTempLogBaseName();
    assertTrue(!tempLog.isEmpty());

    assertLogContainsOriginatingRequestId(gossipServer1, originatingRequestId);
    assertLogContainsOriginatingRequestId(gossipServer2, originatingRequestId);
  }

  //
  // Private
  //

  private static void assertLogContainsOriginatingRequestId(BrikarProcess serverProcess,
                                                            String originatingRequestId) throws IOException {
    final File activeTempLog1 = serverProcess.getActiveTempLog();
    assertNotNull("activeTempLog1 should not be null", activeTempLog1);
    final List<LogMessage> logMessages = new LogParser().parse(activeTempLog1);

    boolean foundOriginatingRequestId = false;
    final StringBuilder logLines = new StringBuilder(4000);
    for (final LogMessage logMessage : logMessages) {
      for (final String line : logMessage.getLines()) {
        logLines.append("\t> ").append(line).append(System.lineSeparator());
        if (line.contains(originatingRequestId)) {
          foundOriginatingRequestId = true;
        }
      }
    }

    assertTrue("Oringinating request ID has not been found in the logs:" + logLines.toString(),
        foundOriginatingRequestId);
  }
}
