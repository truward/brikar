package com.truward.brikar.rpc.support;

import com.truward.brikar.rpc.ServletRpcBinding;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.util.StreamUtils;

import javax.annotation.Generated;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Alexander Shabanov
 */
public final class DefaultServletRpcBindingTest {
  private ServletRpcBinding rpcBinding;
  private DefaultService defaultService;

  @Before
  public void init() {
    defaultService = new DefaultService();
    rpcBinding = new DefaultServletRpcBinding(
        Collections.<HttpMessageConverter<?>>singletonList(new TestHttpMessageConverter()),
        DerivedService.class,
        defaultService);
  }

  @Test
  public void shouldBindGetFoo() throws Exception {
    // Given:
    final MockHttpServletRequest request = new MockHttpServletRequest("POST", "http://localhost:9001/Service");
    request.setParameter("m", "getFoo");
    request.setContentType(MediaType.APPLICATION_JSON_VALUE);
    request.setContent(("Bar,test").getBytes(StandardCharsets.UTF_8));
    final MockHttpServletResponse response = new MockHttpServletResponse();

    // When:
    rpcBinding.process(request, response);

    // Then:
    final String last = defaultService.getLast();
    assertEquals("Foo+test", last);
  }

  //
  // Private
  //

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
      return recordLast(new Bar().setProp("Bar+" + foo.getProp()));
    }

    public String getLast() {
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
    public T setProp(String prop) {
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

    public TestHttpMessageConverter() {
      super(MediaType.APPLICATION_JSON);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
      return DomainObject.class.isAssignableFrom(clazz);
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
      if (!(o instanceof DomainObject)) {
        throw new IOException("unsupported object type");
      }

      outputMessage.getBody().write((o.getClass().getSimpleName() + ',' + ((DomainObject) o).getProp())
          .getBytes(StandardCharsets.UTF_8));
    }
  }
}