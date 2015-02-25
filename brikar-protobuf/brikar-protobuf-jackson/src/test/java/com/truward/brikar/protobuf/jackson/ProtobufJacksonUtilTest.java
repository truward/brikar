package com.truward.brikar.protobuf.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.truward.brikar.protobuf.test.AddressBookModel;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.junit.Assert.assertNotNull;

public class ProtobufJacksonUtilTest {

  @Test
  public void shouldWriteAsJson() throws IOException {
    // Given:
    final AddressBookModel.Person person = AddressBookModel.Person.newBuilder()
        .setId(1).setName("n").setEmail("e")
        .addPhone(AddressBookModel.Person.PhoneNumber.newBuilder()
            .setType(AddressBookModel.Person.PhoneType.HOME)
            .setNumber("111")
            .build())
        .build();

    // When:
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (JsonGenerator jg = jsonGenerator(os)) {
      ProtobufJacksonUtil.writeJson(person, jg);
    }

    // Then:
    final String json = os.toString("UTF-8");
    assertNotNull(json);
  }

  //
  // Private
  //

  @Nonnull
  private static JsonGenerator jsonGenerator(@Nonnull OutputStream outputStream) throws IOException {
    final JsonFactory jsonFactory = new JsonFactory();
    return jsonFactory.createGenerator(outputStream);
  }
}
