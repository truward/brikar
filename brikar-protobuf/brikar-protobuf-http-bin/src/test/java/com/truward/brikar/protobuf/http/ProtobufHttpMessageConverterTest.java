package com.truward.brikar.protobuf.http;

import com.truward.brikar.protobuf.test.AddressBookModel;
import org.junit.Test;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Shabanov
 */
public final class ProtobufHttpMessageConverterTest {

  private final HttpMessageConverter<Object> converter = new ProtobufHttpMessageConverter();

  @Test
  public void shouldBeAbleToReadAndWriteProtobufModel() {
    assertTrue("should be able to read protobuf class",
        converter.canRead(AddressBookModel.AddressBook.class, ProtobufHttpConstants.PROTOBUF_MEDIA_TYPE));
    assertTrue("should be able to write protobuf object",
        converter.canWrite(AddressBookModel.AddressBook.class, ProtobufHttpConstants.PROTOBUF_MEDIA_TYPE));
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
    MockHttpOutputMessage message = new MockHttpOutputMessage();
    converter.write(model, ProtobufHttpConstants.PROTOBUF_MEDIA_TYPE, message);

    // Then:
    final Object deserializedModel = converter.read(AddressBookModel.AddressBook.class,
        new MockHttpInputMessage(message.getBodyAsBytes()));
    assertEquals(model, deserializedModel);
  }
}
