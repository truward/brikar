package com.truward.brikar.protobuf.http.json;

import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Spring's HTTP message converter for protocol buffer messages in JSON form.
 *
 * @author Alexander Shabanov
 */
public class ProtobufJsonHttpMessageConverter extends AbstractHttpMessageConverter<Object> {
  private final JsonFormat.Printer printer;
  private final JsonFormat.Parser parser;
  private final Charset defaultCharset;

  public ProtobufJsonHttpMessageConverter(@Nonnull JsonFormat.Printer printer,
                                          @Nonnull JsonFormat.Parser parser,
                                          @Nonnull Charset defaultCharset) {
    super(MediaType.APPLICATION_JSON);
    this.printer = printer;
    this.parser = parser;
    this.defaultCharset = defaultCharset;
  }

  public ProtobufJsonHttpMessageConverter(@Nonnull JsonFormat.Printer printer, @Nonnull JsonFormat.Parser parser) {
    this(printer, parser, StandardCharsets.UTF_8);
  }

  public ProtobufJsonHttpMessageConverter() {
    this(JsonFormat.printer(), JsonFormat.parser());
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return MessageOrBuilder.class.isAssignableFrom(clazz);
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
    final Charset charset = getContentTypeCharset(inputMessage.getHeaders().getContentType());

    final Message message;
    try {
      message = getDefaultInstance(clazz);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IOException("Reflection Error: unable to read an instance of " + clazz, e);
    }

    final Message.Builder builder = message.toBuilder();

    try (final Reader reader = new InputStreamReader(inputMessage.getBody(), charset)) {
      parser.merge(reader, builder);
      return builder.build();
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
    if (!(o instanceof MessageOrBuilder)) {
      throw new IOException("Expected type=" + MessageOrBuilder.class + " got object=" + o);
    }

    final MessageOrBuilder messageOrBuilder = (MessageOrBuilder) o;
    final Charset charset = getContentTypeCharset(outputMessage.getHeaders().getContentType());

    try (final Writer writer = new OutputStreamWriter(outputMessage.getBody(), charset)) {
      printer.appendTo(messageOrBuilder, writer);
    }
  }

  //
  // Private
  //

  @Nonnull
  public static Message getDefaultInstance(@Nonnull Class<?> messageClass)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

    // use reflection to get descriptor
    final Method method = messageClass.getMethod("getDefaultInstance");
    final Object message = method.invoke(null);
    try {
      return (Message) message;
    } catch (ClassCastException e) {
      throw new IOException("Non-protobuf-generated class: " + messageClass, e);
    }
  }

  private Charset getContentTypeCharset(MediaType contentType) {
    if (contentType != null && contentType.getCharSet() != null) {
      return contentType.getCharSet();
    }
    else {
      return this.defaultCharset;
    }
  }
}
