package com.truward.brikar.protobuf.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Utility class for protobuf->jackson bridge and vice versa.
 *
 * @author Alexander Shabanov
 */
public final class ProtobufJacksonUtil {
  private ProtobufJacksonUtil() {} // hidden

  @Nonnull
  public static <T extends Message> T readJson(@Nonnull Class<T> messageClass, @Nonnull JsonParser jp) throws IOException {
    final Message message;
    try {
      message = getDefaultInstance(messageClass);
    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
      throw new IOException("Reflection Error: unable to read an instance of " + messageClass, e);
    }

    // prepare message data
    final Descriptors.Descriptor descriptor = message.getDescriptorForType();

    // construct the result object
    jp.nextToken();
    final Message result = readMessage(descriptor, jp);
    return messageClass.cast(result);
  }

  public static void writeJson(@Nonnull Message message, @Nonnull JsonGenerator jg) throws IOException {
    final Descriptors.Descriptor descriptor = message.getDescriptorForType();

    jg.writeStartObject();
    for (final Descriptors.FieldDescriptor fieldDescriptor : descriptor.getFields()) {
      if (!(fieldDescriptor.isRepeated() || fieldDescriptor.isRequired()) && !message.hasField(fieldDescriptor)) {
        continue;
      }

      jg.writeFieldName(fieldDescriptor.getName());
      final Object value = message.getField(fieldDescriptor);
      if (!writeValue(value, jg)) {
        throw new IOException("Unable to serialize field '" + fieldDescriptor.getName() + "' in " + message +
            ": unhandled field value");
      }
    }
    jg.writeEndObject();
  }

  public static boolean writeValue(@Nullable Object object, @Nonnull JsonGenerator jg) throws IOException {
    if (object == null) {
      throw new IOException("Protobuf error: can't have null fields");
    }

    if (object instanceof Message) {
      writeJson((Message) object, jg);
      return true;
    }

    if (object instanceof ByteString) {
      final ByteString byteString = (ByteString) object;
      try (final InputStream inputStream = byteString.newInput()) {
        jg.writeBinary(inputStream, byteString.size());
      }
    }

    if ((object instanceof String) || (object instanceof Number) || (object instanceof Boolean)) {
      jg.writeObject(object); // let jackson decide
      return true;
    }

    if (object instanceof Iterable) {
      jg.writeStartArray();
      final Iterable<?> iterable = (Iterable) object;
      for (final Object value : iterable) {
        writeValue(value, jg);
      }
      jg.writeEndArray();
      return true;
    }

    if (object instanceof Descriptors.EnumValueDescriptor) {
      final Descriptors.EnumValueDescriptor enumVal = (Descriptors.EnumValueDescriptor) object;
      jg.writeString(enumVal.getName());
      return true;
    }

    return false;
  }

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

  //
  // Private
  //



  @Nonnull
  private static Message readMessage(@Nonnull Descriptors.Descriptor descriptor, @Nonnull JsonParser jp) throws IOException {
    final Message.Builder builder = descriptor.getOptions().newBuilderForType();

    // make sure current token is the start object
    JsonToken token = jp.getCurrentToken();
    if (token != JsonToken.START_OBJECT) {
      throw new JsonParseException("Object expected", jp.getCurrentLocation());
    }

    // read fields
    for (token = jp.nextToken(); token != JsonToken.END_OBJECT; token = jp.nextToken()) {
      if (token != JsonToken.FIELD_NAME) {
        throw new JsonParseException("Field name expected", jp.getCurrentLocation()); // unlikely
      }

      // get field name
      final String fieldName = jp.getText();

      // advance to the next token (field value)
      jp.nextToken();

      final Descriptors.FieldDescriptor field = descriptor.findFieldByName(fieldName);
      if (field == null) {
        // TODO: handle unknown fields
        throw new JsonParseException("Unknown field " + fieldName, jp.getCurrentLocation());
      }

      final Object object = readObject(field, jp);
      builder.setField(field, object);
    }

    // construct the result object
    final Message result = builder.build();
    if (result == null) {
      throw new IllegalStateException("Builder failed to construct a message of type " + descriptor); // unlikely
    }
    return result;
  }

  @Nonnull
  private static Object readObject(@Nonnull Descriptors.FieldDescriptor descriptor, @Nonnull JsonParser jp) throws IOException {
    switch (descriptor.getJavaType()) {
      case INT:case LONG:case FLOAT:case DOUBLE:
        return jp.getNumberValue();

      case BOOLEAN:
        return jp.getBooleanValue();

      case STRING:
        return jp.getText();

      case BYTE_STRING:
        return jp.getBinaryValue();

      case ENUM:
        throw new UnsupportedOperationException();

      case MESSAGE:
        throw new UnsupportedOperationException();

      default:
        throw new IOException("Unknown descriptor=" + descriptor);
    }
  }
}
