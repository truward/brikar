package com.truward.brikar.error.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.truward.brikar.error.model.ErrorModel;
import com.truward.brikar.protobuf.http.ProtobufHttpConstants;
import com.truward.protobuf.jackson.ProtobufJacksonUtil;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpStatusCodeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Helper class for parsing brikar error from HTTP status code exception.
 *
 * @author Alexander Shabanov
 */
public final class RestErrorParser {
  private RestErrorParser() {}

  public static ErrorModel.ErrorV1 parseError(HttpStatusCodeException exception) throws IOException {
    final MediaType contentType = exception.getResponseHeaders().getContentType();
    if (contentType == null) {
      throw new IOException("Missing content type in the response headers");
    }

    try (final ByteArrayInputStream bais = new ByteArrayInputStream(exception.getResponseBodyAsByteArray())) {
      if (ProtobufHttpConstants.PROTOBUF_MEDIA_TYPE.isCompatibleWith(contentType)) {
        return ErrorModel.ErrorResponseV1.parseDelimitedFrom(bais).getError();
      }

      if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
        final JsonFactory jsonFactory = new JsonFactory();
        try (final JsonParser jp = jsonFactory.createParser(bais)) {
          return ProtobufJacksonUtil.readJson(ErrorModel.ErrorResponseV1.class, jp).getError();
        }
      }

      throw new IOException("Unrecognized contentType=" + contentType);
    }
  }
}
