package com.truward.brikar.protobuf.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
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
  public static Descriptors.Descriptor getDescriptorForClass(@Nonnull Class<?> clazz)
      throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, IOException {

    // use reflection to get descriptor
    final Method method = clazz.getMethod("getDescriptor");
    final Object descriptor = method.invoke(null);
    try {
      return (Descriptors.Descriptor) descriptor;
    } catch (ClassCastException e) {
      throw new IOException("Unable to convert an instance of the returned descriptor: " +
          "non-protobuf-generated class?", e);
    }
  }
}
