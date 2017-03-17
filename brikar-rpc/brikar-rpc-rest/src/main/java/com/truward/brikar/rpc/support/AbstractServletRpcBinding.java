package com.truward.brikar.rpc.support;

import com.truward.brikar.error.model.ErrorModel;
import com.truward.brikar.rpc.RpcMethod;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for RPC bindings.
 */
public abstract class AbstractServletRpcBinding implements ServletRpcBinding {
  public static final String DEFAULT_SERVICE_NAME = "default";

  protected final Logger log = LoggerFactory.getLogger(getClass());

  private final List<HttpMessageConverter<?>> messageConverters;
  private String serviceName = DEFAULT_SERVICE_NAME;

  public AbstractServletRpcBinding(List<HttpMessageConverter<?>> messageConverters) {
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
  }

  public final void setServiceName(@Nonnull String serviceName) {
    this.serviceName = serviceName;
  }

  @Nonnull
  @Override
  public final String getServiceName() {
    return serviceName;
  }

  @Override
  public final void process(@Nullable String urlMethodPath,
                            @Nonnull HttpServletRequest request,
                            @Nonnull HttpServletResponse response) throws IOException {
    // check method - only POST methods are allowed
    if (!HttpMethod.POST.name().equals(request.getMethod())) {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return;
    }

    // get content type
    final MediaType contentType = getMediaType(request, "Content-Type");
    if (contentType == null) {
      response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
      return;
    }

    // get accept type
    MediaType acceptType = getMediaType(request, "Accept");
    acceptType = (acceptType != null ? acceptType : contentType);

    // get method name
    RpcMethod rpcMethod = getRpcMethod(urlMethodPath);
    if (rpcMethod == null) {
      sendError(acceptType, response, HttpServletResponse.SC_NOT_FOUND, "Method not found");
      return;
    }

    // invoke service method
    final Object arg1;
    try {
      arg1 = read(contentType, rpcMethod.getInputType(), new ServletServerHttpRequest(request));
    } catch (HttpMessageNotReadableException e) {
      log.debug("Can't read input message", e);
      sendError(acceptType, response, HttpServletResponse.SC_BAD_REQUEST,
          "Can't read input message");
      return;
    }

    // actually call that method
    try {
      final Object result = rpcMethod.call(arg1);

      // write response
      write(acceptType, rpcMethod.getOutputType(), result,
          new ServletServerHttpResponse(response));
    } catch (Exception e) {
      mapException(e, acceptType, response);
    }

  }

  //
  // Protected
  //

  protected void mapException(Exception e, MediaType acceptType, HttpServletResponse response) throws IOException {
    log.debug("Error", e);
    sendError(acceptType, response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal error");
  }

  @Nullable
  protected RpcMethod getRpcMethod(String urlMethodPath) {
    for (final RpcMethod rpcMethod : getExposedMethods()) {
      if (urlMethodPath.equals(rpcMethod.getName())) {
        return rpcMethod;
      }
    }

    return null;
  }

  protected void sendError(MediaType acceptType,
                           HttpServletResponse response,
                           int statusCode,
                           String description) throws IOException {
    response.setStatus(statusCode);
    write(acceptType,
        ErrorModel.ErrorResponseV2.class,
        ErrorModel.ErrorResponseV2.newBuilder()
            .setError(ErrorModel.ErrorV2.newBuilder()
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

  // VisibleForTesting
  static MediaType getMediaType(HttpServletRequest request, String headerName) {
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
