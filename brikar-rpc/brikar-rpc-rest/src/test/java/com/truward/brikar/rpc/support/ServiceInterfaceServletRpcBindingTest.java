package com.truward.brikar.rpc.support;

import com.truward.brikar.error.model.ErrorV1;
import com.truward.brikar.rpc.ServletRpcBinding;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StreamUtils;

import javax.annotation.Generated;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ServiceInterfaceServletRpcBinding}.
 *
 * @author Alexander Shabanov
 */
public final class ServiceInterfaceServletRpcBindingTest {

  private ServletRpcBinding rpcBinding;
  private DefaultService defaultService;

  @Before
  public void init() {
    defaultService = new DefaultService();
    rpcBinding = new ServiceInterfaceServletRpcBinding(
        Collections.singletonList(new TestHttpMessageConverter()),
        DerivedService.class,
        defaultService);
  }

  @Test
  public void shouldBindGetFoo() throws Exception {
    // Given:
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "http://localhost:9001/Service");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent(("Bar,test").getBytes(StandardCharsets.UTF_8));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    // When:
    rpcBinding.process("getFoo", request, response);

    // Then:
    final String last = defaultService.getLast();
    assertEquals("Foo+test", last);
  }

  @Test
  public void shouldSendNotAllowedCode() throws Exception {
    // Given:
    final MockHttpServletRequest request = new MockHttpServletRequest("GET", "http://localhost:9001/Service");
    final MockHttpServletResponse response = new MockHttpServletResponse();

    // When:
    rpcBinding.process("getFoo", request, response);

    // Then:
    assertEquals(HttpServletResponse.SC_METHOD_NOT_ALLOWED, response.getStatus());
  }

  @Test
  public void shouldSendBadRequestCode() throws Exception {
    // Given:
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "http://localhost:9001/Service");
    request.setContent(("Bar,test").getBytes(StandardCharsets.UTF_8));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    // When:
    rpcBinding.process("getFoo", request, response);

    // Then:
    assertEquals(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, response.getStatus());
  }

  @Test
  public void shouldSendNotFoundCode() throws Exception {
    // Given:
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "http://localhost:9001/Service");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent(("Bar,test").getBytes(StandardCharsets.UTF_8));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    // When:
    rpcBinding.process("getFoo2", request, response);

    // Then:
    assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
  }

  @Test
  public void shouldSendInternalServerError() throws Exception {
    // Given:
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "http://localhost:9001/Service");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent(("Foo,test").getBytes(StandardCharsets.UTF_8));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    // When:
    rpcBinding.process("getBar", request, response);

    // Then:
    assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldFailToInferSingleServiceInterface() {
    new ServiceInterfaceServletRpcBinding(Collections.singletonList(new TestHttpMessageConverter()),
        new NonExposableService());
  }


  @Test
  public void shouldNotFailToInferSingleServiceInterface() {
    new ServiceInterfaceServletRpcBinding(Collections.singletonList(new TestHttpMessageConverter()),
        new DefaultService());
  }

  @Test
  public void shouldOverrideServiceName() {
    // Given:
    final String newServiceName = "OtherService";
    final String oldServiceName = rpcBinding.getServiceName();

    // When:
    ((ServiceInterfaceServletRpcBinding) rpcBinding).setServiceName(newServiceName);

    // Then:
    assertEquals("DerivedService", oldServiceName);
    assertEquals(newServiceName, rpcBinding.getServiceName());
  }

  //
  // Private
  //

  private static final class NonExposableService {}

  private interface BaseService1 {
    @Generated("test") Foo getFoo(Bar bar);
  }

  private interface BaseService2 {
    @Generated("test") Bar getBar(Foo foo);
  }

  private interface DerivedService extends BaseService1, BaseService2 {
    @Generated("test") Baz getBaz(Par par);

    @Generated("test") Par getPar(Baz baz);
  }

  private static final class DefaultService implements DerivedService {
    private List<String> messages = new ArrayList<>();

    @Override
    public Baz getBaz(Par par) {
      return recordLast(new Baz().setProp("Baz+" + par.getProp()));
    }

    @Override
    public Par getPar(Baz baz) {
      return recordLast(new Par().setProp("Par+" + baz.getProp()));
    }

    @Override
    public Foo getFoo(Bar bar) {
      return recordLast(new Foo().setProp("Foo+" + bar.getProp()));
    }

    @Override
    public Bar getBar(Foo foo) {
      throw new UnsupportedOperationException("getBar method is not implemented");
    }

    String getLast() {
      if (messages.isEmpty()) {
        throw new IllegalStateException("Empty messages list");
      }
      return messages.get(messages.size() - 1);
    }

    private <T extends DomainObject> T recordLast(T value) {
      messages.add(value.getProp());
      return value;
    }
  }

  private interface DomainObject {
    String getProp();
  }

  private static abstract class AbstractDomainObject<T extends DomainObject> implements DomainObject {
    private String prop = "default";

    @Override
    public String getProp() {
      return prop;
    }

    @SuppressWarnings("unchecked")
    T setProp(String prop) {
      this.prop = prop;
      return (T) this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof AbstractDomainObject)) return false;

      AbstractDomainObject that = (AbstractDomainObject) o;

      return !(prop != null ? !prop.equals(that.prop) : that.prop != null);

    }

    @Override
    public int hashCode() {
      return prop != null ? prop.hashCode() : 0;
    }

    @Override
    public String toString() {
      return getClass().getSimpleName() + "{" +
          "prop='" + prop + '\'' +
          '}';
    }
  }

  private static final class Foo extends AbstractDomainObject<Foo> {
  }

  private static final class Bar extends AbstractDomainObject<Bar> {
  }

  private static final class Baz extends AbstractDomainObject<Baz> {
  }

  private static final class Par extends AbstractDomainObject<Par> {
  }

  private static final class TestHttpMessageConverter extends AbstractHttpMessageConverter<Object> {

    TestHttpMessageConverter() {
      super(MediaType.APPLICATION_JSON);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
      return DomainObject.class.isAssignableFrom(clazz) || ErrorV1.ErrorResponse.class.isAssignableFrom(clazz);
    }

    @Override
    protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
      final String body = StreamUtils.copyToString(inputMessage.getBody(), StandardCharsets.UTF_8);
      final String[] pairs = body.split(",");
      switch (pairs[0]) {
        case "Foo": return new Foo().setProp(pairs[1]);
        case "Bar": return new Bar().setProp(pairs[1]);
        case "Baz": return new Baz().setProp(pairs[1]);
        case "Par": return new Par().setProp(pairs[1]);
      }

      throw new IOException("Unsupported class type in body=" + body);
    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
      if (o instanceof ErrorV1.ErrorResponse) {
        outputMessage.getBody().write(((ErrorV1.ErrorResponse) o).getError().getMessage()
            .getBytes(StandardCharsets.UTF_8));
        return;
      }

      if (!(o instanceof DomainObject)) {
        throw new IOException("unsupported object type");
      }

      outputMessage.getBody().write((o.getClass().getSimpleName() + ',' + ((DomainObject) o).getProp())
          .getBytes(StandardCharsets.UTF_8));
    }
  }
}
