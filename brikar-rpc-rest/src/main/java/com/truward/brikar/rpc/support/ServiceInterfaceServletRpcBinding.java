package com.truward.brikar.rpc.support;

import com.truward.brikar.error.model.ErrorModel;
import com.truward.brikar.rpc.ServletRpcBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Exposes service proxy in a form of RPC.
 *
 * @author Alexander Shabanov
 */
public class ServiceInterfaceServletRpcBinding implements ServletRpcBinding {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final List<HttpMessageConverter<?>> messageConverters;
  private final Object serviceProxy;
  private String serviceName;

  private final Map<String, Method> methodMap;

  public ServiceInterfaceServletRpcBinding(List<HttpMessageConverter<?>> messageConverters,
                                           Class<?> serviceInterface,
                                           Object serviceProxy) {
    Objects.requireNonNull(messageConverters, "messageConverters");
    final List<HttpMessageConverter<?>> messageConvertersCopy = new ArrayList<>(messageConverters.size());
    for (int i = 0; i < messageConverters.size(); ++i) {
      final HttpMessageConverter<?> messageConverter = messageConverters.get(i);
      if (messageConverter == null) {
        throw new IllegalArgumentException("Null message converter #" + i);
      }
      messageConvertersCopy.add(messageConverter);
    }

    this.messageConverters = messageConvertersCopy;
    this.serviceProxy = Objects.requireNonNull(serviceProxy, "serviceProxy");
    Objects.requireNonNull(serviceInterface, "serviceInterface");

    if (!serviceInterface.isInstance(serviceProxy)) {
      throw new IllegalArgumentException("serviceProxy is not an instance of " + serviceInterface);
    }

    this.methodMap = getCheckedMethodMap(serviceInterface);
    setServiceName(serviceInterface.getSimpleName());
  }

  public ServiceInterfaceServletRpcBinding(List<HttpMessageConverter<?>> messageConverters,
                                           Object serviceProxy) {
    this(messageConverters, getInferredInteraceFromServiceProxy(serviceProxy), serviceProxy);
  }

  public void setServiceName(@Nonnull String serviceName) {
    this.serviceName = serviceName;
  }

  @Nonnull
  @Override
  public String getServiceName() {
    return serviceName;
  }

  @Nonnull
  @Override
  public List<String> getExposedMethodNames() {
    return new ArrayList<>(methodMap.keySet());
  }

  @Override
  public void process(@Nullable String urlMethodPath,
                      @Nonnull HttpServletRequest request,
                      @Nonnull HttpServletResponse response) throws IOException {
    // check method - only POST methods are allowed
    if (!HttpMethod.POST.name().equals(request.getMethod())) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    // get method name
    final Method method = methodMap.get(urlMethodPath);
    if (method == null) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND);
      return;
    }

    // get content type
    final MediaType contentType = getMediaType(request, "Content-Type");
    if (contentType == null) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    // get accept type
    MediaType acceptType = getMediaType(request, "Accept");
    acceptType = (acceptType != null ? acceptType : contentType);

    // invoke service method
    final Object arg1;
    try {
      arg1 = read(contentType, method.getParameterTypes()[0], new ServletServerHttpRequest(request));
    } catch (HttpMessageNotReadableException e) {
      log.debug("Can't read input message", e);
      sendError(acceptType, response, HttpServletResponse.SC_BAD_REQUEST,
          "Can't read input message");
      return;
    }

    final Object result;
    try {
      result = method.invoke(serviceProxy, arg1);
    } catch (IllegalAccessException e) {
      log.error("IllegalAccessException while invoking {}", method);
      sendError(acceptType, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Internal error");
      return;
    } catch (InvocationTargetException e) {
      handleInvocationException(acceptType, response, method, e.getTargetException());
      return;
    }

    // write response
    // TODO: parse Accept header
    write(acceptType, method.getReturnType(), result, new ServletServerHttpResponse(response));
  }

  //
  // Protected
  //

  protected void sendError(MediaType acceptType,
                           HttpServletResponse response,
                           int statusCode,
                           String description) throws IOException {
    response.setStatus(statusCode);
    write(acceptType,
        ErrorModel.ErrorResponseV1.class,
        ErrorModel.ErrorResponseV1.newBuilder()
            .setError(ErrorModel.ErrorV1.newBuilder()
                .setCode(HttpStatus.valueOf(statusCode).name())
                .setMessage(description)
                .build())
            .build(),
        new ServletServerHttpResponse(response));
  }

  protected void handleInvocationException(MediaType acceptType,
                                           HttpServletResponse response,
                                           Method method,
                                           Throwable e) throws IOException {
    // TODO: error mapping
    if (e instanceof IllegalArgumentException) {
      sendError(acceptType, response, HttpServletResponse.SC_BAD_REQUEST,
          "Illegal Argument: " + e.getMessage());
      return;
    }

    log.error("InvocationTargetException while invoking {}", method, e);
    sendError(acceptType, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
        "Internal Error: " + e.getMessage());
  }

  //
  // Private
  //

  private static Class<?> getInferredInteraceFromServiceProxy(Object serviceProxy) {
    Objects.requireNonNull(serviceProxy, "serviceProxy");
    final Class[] interfaces = serviceProxy.getClass().getInterfaces();
    if (interfaces.length != 1) {
      throw new IllegalArgumentException("Can't infer single implementing interface for given proxy of type=" +
          serviceProxy.getClass());
    }
    return interfaces[0];
  }

  private Map<String, Method> getCheckedMethodMap(Class<?> serviceInterface) {
    final Method[] methods = serviceInterface.getMethods();

    final Map<String, Method> methodMap = new HashMap<>(methods.length * 2);
    for (final Method method : methods) {
      if (method.getDeclaringClass().equals(Object.class)) {
        continue; // exclude object methods
      }

      if (method.getParameterTypes().length != 1) {
        throw new IllegalStateException("Only input one parameter supported, offending method=" + method);
      }

      final Method oldMethod = methodMap.put(method.getName(), method);
      if (oldMethod != null) {
        log.warn("Overloaded methods are not supported in serviceInterface={}, method1={}, method2={}",
            serviceInterface, oldMethod, method);
      }
    }

    return methodMap;
  }

  @SuppressWarnings("unchecked")
  private Object read(MediaType mediaType, Class<?> clazz, HttpInputMessage inputMessage) throws IOException {
    for (final HttpMessageConverter<?> messageConverter : messageConverters) {
      if (messageConverter.canRead(clazz, mediaType)) {
        return ((HttpMessageConverter<Object>) messageConverter).read(clazz, inputMessage);
      }
    }

    throw new HttpMessageNotReadableException("Unable to read object of type=" + clazz +
        " with mediaType=" + mediaType);
  }

  @SuppressWarnings("unchecked")
  private void write(MediaType mediaType, Class clazz, Object message, HttpOutputMessage outputMessage) throws IOException {
    for (final HttpMessageConverter<?> messageConverter : messageConverters) {
      if (messageConverter.canWrite(clazz, mediaType)) {
        ((HttpMessageConverter<Object>) messageConverter).write(message, mediaType, outputMessage);
        return;
      }
    }

    throw new HttpMessageNotWritableException("Unable to write object of type=" + clazz +
        " with mediaType=" + mediaType);
  }

  private static MediaType getMediaType(HttpServletRequest request, String headerName) {
    final String mediaTypeName = request.getHeader(headerName);
    if (mediaTypeName == null) {
      return null;
    }

    try {
      return MediaType.valueOf(mediaTypeName);
    } catch (InvalidMediaTypeException ignored) {
      return null;
    }
  }
}
