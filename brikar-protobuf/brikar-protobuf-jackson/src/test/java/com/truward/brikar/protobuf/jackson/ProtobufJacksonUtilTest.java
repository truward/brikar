package com.truward.brikar.protobuf.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.truward.brikar.protobuf.test.AddressBookModel;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.*;

import static org.junit.Assert.assertNotNull;

public final class ProtobufJacksonUtilTest {
  private final AddressBookModel.Person person = AddressBookModel.Person.newBuilder()
      .setId(1).setName("n").setEmail("e")
      .addPhone(AddressBookModel.Person.PhoneNumber.newBuilder()
          .setType(AddressBookModel.Person.PhoneType.HOME)
          .setNumber("111")
          .build())
      .build();

  @Test
  public void shouldWriteAsJson() throws IOException {
    // Given:
    final AddressBookModel.Person person = this.person;

    // When:
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (JsonGenerator jg = jsonGenerator(os)) {
      ProtobufJacksonUtil.writeJson(person, jg);
    }

    // Then:
    final String json = os.toString("UTF-8");
    assertNotNull(json);
  }

  @Ignore
  @Test
  public void shouldNotReadMalformedInput() {
  }

  @Test
  public void shouldReadJson() throws IOException {
    // Given:
    final AddressBookModel.Person person = this.person;

    // When:
    final ByteArrayOutputStream os = new ByteArrayOutputStream();
    try (JsonGenerator jg = jsonGenerator(os)) {
      ProtobufJacksonUtil.writeJson(person, jg);
    }

    final AddressBookModel.Person p2;
    final byte[] b = os.toByteArray();
    System.out.println(os.toString());
    try (JsonParser jp = jsonParser(new ByteArrayInputStream(b))) {
      p2 = ProtobufJacksonUtil.readJson(AddressBookModel.Person.class, jp);
    }

    assertNotNull(p2);
  }

  //
  // Private
  //

  @Nonnull
  private static JsonGenerator jsonGenerator(@Nonnull OutputStream outputStream) throws IOException {
    final JsonFactory jsonFactory = new JsonFactory();
    return jsonFactory.createGenerator(outputStream);
  }

  @Nonnull
  private static JsonParser jsonParser(@Nonnull InputStream inputStream) throws IOException {
    final JsonFactory jsonFactory = new JsonFactory();
    return jsonFactory.createParser(inputStream);
  }
}
