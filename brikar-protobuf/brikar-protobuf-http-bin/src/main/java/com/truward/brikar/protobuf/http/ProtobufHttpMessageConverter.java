package com.truward.brikar.protobuf.http;

import com.google.protobuf.Message;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Message converter for protocol buffer entities.
 *
 * @author Alexander Shabanov
 */
public class ProtobufHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

  private final Map<Class<?>, Method> parseMethods = new ConcurrentHashMap<>();

  public ProtobufHttpMessageConverter() {
    super(ProtobufHttpConstants.PROTOBUF_MEDIA_TYPE);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return Message.class.isAssignableFrom(clazz);
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
    final Method parseMethod = getParseMethod(clazz);
    try {
      return parseMethod.invoke(null, httpInputMessage.getBody());
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(e); // normally shouldn't happen
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
    final Message message = (Message) o;
    message.writeTo(httpOutputMessage.getBody());
  }

  //
  // Private
  //

  private Method getParseMethod(Class<?> clazz) {
    // short circuit
    final Method m = parseMethods.get(clazz);
    if (m != null) {
      return m;
    }

    // find method and put it to the map
    try {
      final Method method = clazz.getMethod("parseFrom", InputStream.class);

      // save to cache
      parseMethods.put(clazz, method);

      // return
      return method;
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("No method parseFrom for class=" + clazz);
    }
  }
}
