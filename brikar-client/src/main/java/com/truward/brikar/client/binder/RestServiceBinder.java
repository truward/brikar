package com.truward.brikar.client.binder;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriTemplate;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexander Shabanov
 */
public class RestServiceBinder {

  private void createMethodHandler(Method method) {

    final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
    if (requestMapping == null) {
      throw new BeanInitializationException("RequestMapping annotation is missing");
    }

    //requestMapping.method() <-- only one supported (or zero - in this case GET implied)
    //requestMapping.value() <-- only one or no mappings expected
    final RequestMethod[] methods = requestMapping.method();

    if (requestMapping.consumes().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.consumes is not supported");
    }

    if (requestMapping.headers().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.headers is not supported");
    }

    if (requestMapping.produces().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.consumes is not supported");
    }
  }

  interface UriExtractor {
    @Nonnull
    URI extract(@Nonnull Object[] arguments);
  }

  private static abstract class AbstractUriExtractor {
    final String baseUrl;

    public AbstractUriExtractor(@Nonnull String baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  private static final class EmptyParamsUriExtractor extends AbstractUriExtractor implements UriExtractor {

    public EmptyParamsUriExtractor(@Nonnull String baseUri) {
      super(baseUri);
    }

    @Nonnull
    @Override
    public URI extract(@Nonnull Object[] arguments) {
      try {
        return new URI(baseUrl);
      } catch (URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static final class StandardUriExtractor extends AbstractUriExtractor implements UriExtractor {
    final PosToNamePair[] variables;

    public StandardUriExtractor(@Nonnull String baseUri, @Nonnull PosToNamePair[] variables) {
      super(baseUri);
      this.variables = variables;
    }

    @Nonnull
    @Override
    public URI extract(@Nonnull Object[] arguments) {
      final UriTemplate uriTemplate = new UriTemplate(baseUrl);
      final Map<String, Object> params = new HashMap<>(variables.length * 2);
      for (final PosToNamePair pair : variables) {
        params.put(pair.argName, arguments[pair.argPos]);
      }
      return uriTemplate.expand(params);
    }
  }

  private static final class PosToNamePair {
    final int argPos;
    final String argName;

    public PosToNamePair(int argPos, String argName) {
      this.argPos = argPos;
      this.argName = argName;
    }
  }
}
