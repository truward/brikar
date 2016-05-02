package com.truward.brikar.protobuf.http.json;

import com.truward.brikar.protobuf.test.AddressBookModel;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.io.IOException;
import java.nio.charset.Charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class ProtobufJsonHttpMessageConverterTest {
  private final HttpMessageConverter<Object> converter = new ProtobufJsonHttpMessageConverter();

  @Test
  public void shouldBeAbleToReadAndWriteProtobufModel() {
    assertTrue("should be able to read protobuf class",
        converter.canRead(AddressBookModel.AddressBook.class, MediaType.APPLICATION_JSON));
    assertTrue("should be able to write protobuf object",
        converter.canWrite(AddressBookModel.AddressBook.class, MediaType.APPLICATION_JSON));
  }

  @Test
  public void shouldSerializeAndDeserializeMessage() throws IOException {
    // Given:
    final AddressBookModel.AddressBook model = AddressBookModel.AddressBook.newBuilder()
        .addPerson(AddressBookModel.Person.newBuilder()
            .setId(1).setEmail("email").setName("name")
            .addPhone(AddressBookModel.Person.PhoneNumber.newBuilder()
                .setNumber("213123").setType(AddressBookModel.Person.PhoneType.MOBILE).build())
            .build())
        .build();

    // When:
    final MockHttpOutputMessage message = new MockHttpOutputMessage();
    converter.write(model, MediaType.APPLICATION_JSON, message);

    // Then:
    final Object deserializedModel = converter.read(AddressBookModel.AddressBook.class,
        new MockHttpInputMessage(message.getBodyAsBytes()));
    assertEquals(model, deserializedModel);
  }

  @Test
  public void shouldDeserializeUtf16Message() throws IOException {
    // Given:
    final String json = "{\"id\": 12, \"email\": \"test@test\", \"name\": \"Test\"}";
    final Charset charset = Charset.forName("UTF-16");
    final byte[] jsonBytes = json.getBytes(charset);
    final MockHttpInputMessage message = new MockHttpInputMessage(jsonBytes);
    message.getHeaders().setContentType(new MediaType("application", "json", charset));

    // When:
    final Object deserializedModel = converter.read(AddressBookModel.Person.class, message);

    // Then:
    assertEquals(AddressBookModel.Person.newBuilder()
        .setId(12).setEmail("test@test").setName("Test")
        .build(), deserializedModel);
  }
}
