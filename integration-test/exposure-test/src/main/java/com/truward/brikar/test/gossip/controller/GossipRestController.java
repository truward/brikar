package com.truward.brikar.test.gossip.controller;

import com.google.protobuf.StringValue;
import com.truward.brikar.test.gossip.model.GossipChainMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Objects;

/**
 * @author Alexander Shabanov
 */
@Controller
@RequestMapping("/rest")
public final class GossipRestController {
  public static final String ABOUT_PARAM = "about";

  private String gossipToken = "?";
  private GossipChainMode gossipChainMode = GossipChainMode.END;
  private URI gossipUri;
  private RestOperations restClient;

  public void setGossipToken(@Nonnull String gossipToken) {
    this.gossipToken = Objects.requireNonNull(gossipToken);
  }

  public void setGossipChainMode(@Nonnull GossipChainMode mode) {
    this.gossipChainMode = Objects.requireNonNull(mode);
  }

  public void setGossipUri(@Nullable URI gossipUri) {
    this.gossipUri = Objects.requireNonNull(gossipUri);
  }

  public void setRestClient(@Nullable RestOperations restClient) {
    this.restClient = restClient;
  }

  @RequestMapping(value = "/gossip", method = RequestMethod.GET)
  @ResponseBody
  public StringValue gossip(@RequestParam(name = "about") String about) {
    final String gossipValue = gossipToken + "-" + about;

    final StringValue.Builder resultBuilder = StringValue.newBuilder();
    switch (gossipChainMode) {
      case NEXT:
        final RestOperations restClient = this.restClient;
        final URI gossipUri = this.gossipUri;
        if (gossipUri == null || restClient == null) {
          throw new IllegalStateException("Can't call next node in gossip chain: gossipUri or restClient is null");
        }

        final StringValue chainGossip = restClient.getForObject(UriComponentsBuilder.fromUri(gossipUri)
            .queryParam(ABOUT_PARAM, gossipValue)
            .build()
            .toUri(), StringValue.class);

        resultBuilder.setValue(chainGossip.getValue());

        break;

      case END:
        resultBuilder.setValue(gossipValue);
        break;
    }

    return resultBuilder.build();
  }
}
