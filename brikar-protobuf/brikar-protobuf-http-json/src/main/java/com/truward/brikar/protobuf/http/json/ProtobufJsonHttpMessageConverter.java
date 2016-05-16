package com.truward.brikar.protobuf.http.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.google.protobuf.Message;
import com.truward.protobuf.jackson.ProtobufJacksonUtil;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Spring's HTTP message converter for protocol buffer messages in JSON form.
 *
 * @author Alexander Shabanov
 */
public class ProtobufJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
  private final JsonFactory jsonFactory;

  public ProtobufJsonHttpMessageConverter(@Nonnull JsonFactory jsonFactory) {
    super(MediaType.APPLICATION_JSON);
    this.jsonFactory = jsonFactory;
  }

  public ProtobufJsonHttpMessageConverter() {
    this(new JsonFactory());
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return Message.class.isAssignableFrom(clazz);
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
    final Class<? extends Message> messageClass = clazz.asSubclass(Message.class);
    try (final JsonParser jp = jsonFactory.createParser(inputMessage.getBody())) {
      return ProtobufJacksonUtil.readJson(messageClass, jp);
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
    try (final JsonGenerator jg = jsonFactory.createGenerator(outputMessage.getBody())) {
      ProtobufJacksonUtil.writeJson((Message) o, jg);
    }
  }
}
