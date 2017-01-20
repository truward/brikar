package com.truward.brikar.client.rest.support;

import com.truward.brikar.client.rest.RestServiceBinder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestOperations;
import org.springframework.web.util.UriTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @author Alexander Shabanov
 */
public class StandardRestServiceBinder implements RestServiceBinder {
  private final RestOperations restOperations;

  public StandardRestServiceBinder(@Nonnull RestOperations restOperations) {
    this.restOperations = Objects.requireNonNull(restOperations, "restOperations");
  }

  @Override
  @Nonnull
  public <T> T createClient(@Nonnull String serviceBaseUrl,
                            @Nonnull Class<T> restServiceClass,
                            @Nonnull Class<?> ... extraClasses) {
    final Map<Method, MethodInvocationHandler> handlerMap = new HashMap<>();
    for (final Method method : restServiceClass.getMethods()) {
      handlerMap.put(method, getMethodInvocationHandler(method, restServiceClass, serviceBaseUrl));
    }

    final Class[] classes = Arrays.copyOf(extraClasses, extraClasses.length + 1);
    classes[extraClasses.length] = restServiceClass;

    final Object o = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
        classes, new MethodMapBasedInvocationHandler(handlerMap));
    return restServiceClass.cast(o);
  }

  @Override
  @Nonnull
  public <T> T createClient(@Nonnull URI serviceBaseUrl,
                     @Nonnull Class<T> restServiceClass,
                     @Nonnull Class<?> ... extraClasses) {
    return createClient(serviceBaseUrl.toString(), restServiceClass, extraClasses);
  }

  //
  // Private
  //

  /*
   * Creates an instance of an interface which is supposed to handle method calls in the proxy.
   */
  @Nonnull
  private MethodInvocationHandler getMethodInvocationHandler(@Nonnull Method method,
                                                             @Nonnull final Class<?> clazz,
                                                             @Nonnull String serviceBaseUrl) {
    // toString+hashCode+equals
    if (method.getName().equals("toString") && method.getParameterTypes().length == 0) {
      return (proxy, method1, args) -> clazz.getSimpleName();
    } else if (method.getName().equals("hashCode") && method.getParameterTypes().length == 0) {
      return new MethodInvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
          return this.hashCode();
        }
      };
    } else if (method.getName().equals("equals") && method.getParameterTypes().length == 1 &&
        method.getParameterTypes()[0] == Object.class) {
      return (proxy, method12, args) -> proxy == args[0];
    }

    return createRequestMappingHandler(method, serviceBaseUrl);
  }

  private interface MethodInvocationHandler {
    Object invoke(Object proxy, Method method, Object[] args) throws Throwable;
  }

  private static class MethodMapBasedInvocationHandler implements InvocationHandler {
    final Map<Method, MethodInvocationHandler> handlers;

    public MethodMapBasedInvocationHandler(@Nonnull Map<Method, MethodInvocationHandler> handlers) {
      this.handlers = handlers;
    }

    @Override
    public Object invoke(Object proxy,
                         @SuppressWarnings("NullableProblems") Method method,
                         Object[] args) throws Throwable {
      final MethodInvocationHandler handler = handlers.get(method);
      if (handler == null) {
        throw new UnsupportedOperationException("Unsupported method=" + method);
      }
      return handler.invoke(proxy, method, args);
    }
  }

  @Nonnull
  private MethodInvocationHandler createRequestMappingHandler(@Nonnull Method method, @Nonnull String serviceBaseUrl) {

    final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
    if (requestMapping == null) {
      // Request mapping is missing - just POST to the given URL with method as a parameter
      return new RestMethodInvocationHandler(HttpMethod.POST,
          new PostRequestUriExtractor(serviceBaseUrl, method.getName()),
          PostRequestBodyExtractor.INSTANCE,
          restOperations);
    }

    checkUnsupportedProperties(requestMapping);
    final RequestMethod requestMethod = getRequestMethod(requestMapping);
    final MethodParseResult parseResult = parseMethod(method, serviceBaseUrl, requestMapping);

    return new RestMethodInvocationHandler(HttpMethod.valueOf(requestMethod.name()), parseResult.uriExtractor,
        parseResult.bodyExtractor, restOperations);
  }

  @Nonnull
  private MethodParseResult parseMethod(@Nonnull Method method,
                                        @Nonnull String serviceBaseUrl,
                                        @Nonnull RequestMapping requestMapping) {
    final Annotation[][] methodParamAnnotations = method.getParameterAnnotations();

    final List<PosNamePair> posNamePairs = new ArrayList<>(10);
    final String prefixUrl = getRequestMappingPrefixUrl(requestMapping);
    final StringBuilder builder = new StringBuilder(serviceBaseUrl.length() + prefixUrl.length() + 50);
    RequestBodyExtractor bodyExtractor = EmptyRequestBodyExtractor.INSTANCE;

    builder.append(serviceBaseUrl).append(prefixUrl);

    // iterate over each method parameter's annotation
    for (int i = 0, rp = 0; i < methodParamAnnotations.length; ++i) {
      final Annotation[] annotations = methodParamAnnotations[i];
      for (final Annotation a : annotations) {
        if (a.annotationType().equals(PathVariable.class)) {
          posNamePairs.add(new PosNamePair(i, ((PathVariable) a).value()));
          continue;
        } else if (a.annotationType().equals(RequestParam.class)) {
          final String name = ((RequestParam) a).value();
          posNamePairs.add(new PosNamePair(i, name));

          // since this is a request param we need to modify URL template as well
          // this is needed to make UriTemplate handle query parameters in addition to path parameters
          builder.append(rp > 0 ? '&' : '?').append(name).append('=').append(getParamMask(builder.toString(), name));

          ++rp;
          continue;
        } else if (a.annotationType().equals(RequestBody.class)) {
          if (bodyExtractor != EmptyRequestBodyExtractor.INSTANCE) {
            // more than one request body? - possible wrong mapping
            throw new UnsupportedOperationException("Can not have more than one RequestBody, offending " +
                "parameter number: " + (i + 1));
          }

          bodyExtractor = new PosArgBodyExtractor(i);
          continue;
        }

        throw new UnsupportedOperationException("Unable to create method handler because of parameter number "  +
            (i + 1) + ": PathVariable, RequestParam or RequestBody annotation is expected");
      }
    }

    final UriExtractor uriExtractor;
    if (posNamePairs.isEmpty()) {
      // optimized version
      uriExtractor = new EmptyParamsUriExtractor(builder.toString());
    } else {
      uriExtractor = new StandardUriExtractor(builder.toString(), posNamePairs.toArray(new PosNamePair[posNamePairs.size()]));
    }

    return new MethodParseResult(uriExtractor, bodyExtractor);
  }

  private static String getParamMask(@Nonnull String partialUrl, @Nonnull String argName) {
    final StringBuilder mask = new StringBuilder(argName.length() + 3);
    for (int i = 0;; ++i) {
      mask.setLength(0);
      mask.append('{').append(argName);
      if (i > 0) {
        mask.append(i);
      }
      mask.append('}');

      if (!partialUrl.contains(mask)) {
        return mask.toString();
      }
    }
  }

  private static final class MethodParseResult {
    final UriExtractor uriExtractor;
    final RequestBodyExtractor bodyExtractor;

    public MethodParseResult(@Nonnull UriExtractor uriExtractor, @Nonnull RequestBodyExtractor bodyExtractor) {
      this.uriExtractor = uriExtractor;
      this.bodyExtractor = bodyExtractor;
    }
  }

  @Nonnull
  private String getRequestMappingPrefixUrl(@Nonnull RequestMapping requestMapping) {
    final String[] mappings = requestMapping.value();
    if (mappings.length == 1) {
      return mappings[0];
    } else if (mappings.length == 0) {
      return "";
    }

    throw new UnsupportedOperationException("RequestMapping.value is not expected to have more than one value");
  }

  private void checkUnsupportedProperties(@Nonnull RequestMapping requestMapping) {
    if (requestMapping.consumes().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.consumes is not supported");
    }

    if (requestMapping.headers().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.headers is not supported");
    }

    if (requestMapping.produces().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.consumes is not supported");
    }

    if (requestMapping.params().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.params is not supported");
    }
  }

  @Nonnull
  private RequestMethod getRequestMethod(@Nonnull RequestMapping requestMapping) {
    final RequestMethod[] methods = requestMapping.method();
    if (methods.length == 1) {
      return methods[0];
    } else if (methods.length == 0) {
      return RequestMethod.GET; // default method
    }

    throw new UnsupportedOperationException("RequestMapping.methods contains more than one parameters");
  }

  private static final class RestMethodInvocationHandler implements MethodInvocationHandler {
    final HttpMethod httpMethod;
    final UriExtractor uriExtractor;
    final RequestBodyExtractor bodyExtractor;
    final RestOperations restOperations;

    public RestMethodInvocationHandler(@Nonnull HttpMethod httpMethod,
                                       @Nonnull UriExtractor uriExtractor,
                                       @Nonnull RequestBodyExtractor bodyExtractor,
                                       @Nonnull RestOperations restOperations) {
      this.httpMethod = httpMethod;
      this.uriExtractor = uriExtractor;
      this.bodyExtractor = bodyExtractor;
      this.restOperations = restOperations;
    }

    @Override
    public Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
      final URI uri = uriExtractor.extract(args);
      final Object body = bodyExtractor.extract(args);

      HttpEntity<?> entity;
      if (body == null) {
        entity = HttpEntity.EMPTY;
      } else {
        entity = new HttpEntity<>(body);
      }

      // NOTE: exchange may throw HttpServerErrorException and HttpClientErrorException
      final ResponseEntity<?> responseEntity = restOperations.exchange(uri, httpMethod, entity, method.getReturnType());
      if (responseEntity == null) {
        throw new IllegalStateException("responseEntity is null"); // should not happen
      }
      return responseEntity.getBody();
    }
  }

  //
  // RequestBodyExtractor
  //

  interface RequestBodyExtractor {
    @Nullable
    Object extract(@Nullable Object[] arguments);
  }

  private static final class PostRequestBodyExtractor implements RequestBodyExtractor {

    public static final RequestBodyExtractor INSTANCE = new PostRequestBodyExtractor();

    private PostRequestBodyExtractor() {
    }

    @Nullable
    @Override
    public Object extract(@Nullable Object[] arguments) {
      if (arguments == null || arguments.length != 1) {
        throw new IllegalStateException("Exactly one argument expected for methods that are not " +
            "annotated with RequestMapping annotation");
      }
      return arguments[0];
    }
  }

  private static final class EmptyRequestBodyExtractor implements RequestBodyExtractor {
    static final EmptyRequestBodyExtractor INSTANCE = new EmptyRequestBodyExtractor();

    @Nullable
    @Override
    public Object extract(@Nullable Object[] arguments) {
      return null;
    }
  }

  private static final class PosArgBodyExtractor implements RequestBodyExtractor {
    final int argPos;

    public PosArgBodyExtractor(int argPos) {
      this.argPos = argPos;
    }

    @Nullable
    @Override
    public Object extract(@Nullable Object[] arguments) {
      if (arguments == null || argPos >= arguments.length) {
        // should never happen as we check positional arguments before constructing this object
        throw new IllegalStateException("Unexpected arguments: too few given");
      }
      return arguments[argPos];
    }
  }

  //
  // UriExtractor
  //

  interface UriExtractor {
    @Nonnull
    URI extract(@Nullable Object[] arguments);
  }

  private static abstract class AbstractUriExtractor implements UriExtractor {
    final String baseUrl;

    public AbstractUriExtractor(@Nonnull String baseUrl) {
      this.baseUrl = baseUrl;
    }
  }

  private static final class PostRequestUriExtractor extends AbstractUriExtractor {
    final String methodName;

    public PostRequestUriExtractor(@Nonnull String baseUrl, @Nonnull String methodName) {
      super(baseUrl);
      this.methodName = methodName;
    }

    @Nonnull
    @Override
    public URI extract(@Nullable Object[] arguments) {
      // suppressed to have StringBuilder with estimated length
      //noinspection StringBufferReplaceableByString
      final StringBuilder builder = new StringBuilder(1 + baseUrl.length() + methodName.length());
      builder.append(baseUrl).append('/').append(methodName);
      return URI.create(builder.toString());
    }
  }

  private static final class EmptyParamsUriExtractor extends AbstractUriExtractor {

    public EmptyParamsUriExtractor(@Nonnull String baseUri) {
      super(baseUri);
    }

    @Nonnull
    @Override
    public URI extract(@Nullable Object[] arguments) {
      try {
        return new URI(baseUrl);
      } catch (URISyntaxException e) {
        throw new IllegalStateException(e);
      }
    }
  }

  private static final class StandardUriExtractor extends AbstractUriExtractor {
    final PosNamePair[] variables;

    public StandardUriExtractor(@Nonnull String baseUri, @Nonnull PosNamePair[] variables) {
      super(baseUri);
      this.variables = variables;
    }

    @Nonnull
    @Override
    public URI extract(@Nullable Object[] arguments) {
      final UriTemplate uriTemplate = new UriTemplate(baseUrl);

      if (variables.length == 0) {
        return uriTemplate.expand();
      }

      if (arguments == null || arguments.length != variables.length) {
        // should never happen
        throw new IllegalStateException("Unexpected: too few arguments given");
      }

      final Map<String, Object> params = new HashMap<>(variables.length * 2);
      for (final PosNamePair pair : variables) {
        params.put(pair.argName, arguments[pair.argPos]);
      }

      return uriTemplate.expand(params);
    }
  }

  private static final class PosNamePair {
    final int argPos;
    final String argName;

    public PosNamePair(int argPos, String argName) {
      this.argPos = argPos;
      this.argName = argName;
    }
  }
}
