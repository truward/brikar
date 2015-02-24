package com.truward.brikar.protobuf.http;

import org.springframework.http.MediaType;

/**
 * Common HTTP-related constants for protobuf REST API.
 *
 * @author Alexander Shabanov
 */
public final class ProtobufHttpConstants {
  private ProtobufHttpConstants() {} // hidden

  /**
   * Media type for protocol buffers-based REST API, standard protobuf serialization implied.
   */
  public static final MediaType PROTOBUF_MEDIA_TYPE = new MediaType("application", "x-protobuf");
}
