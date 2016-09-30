package com.truward.brikar.client.rest.support;

import com.truward.brikar.client.binder.RestServiceBinder;
import com.truward.brikar.client.binder.RestServiceBinderFactory;
import com.truward.brikar.client.interceptor.RequestLogAwareHttpRequestInterceptor;
import com.truward.brikar.client.interceptor.RequestLogAwareHttpResponseInterceptor;
import com.truward.brikar.client.rest.RestClientBuilderFactory;
import com.truward.brikar.client.rest.RestClientBuilder;
import org.apache.http.*;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Default implementation of {@link RestClientBuilderFactory}, adapted for spring framework.
 *
 * @author Alexander Shabanov
 */
public class StandardRestClientBuilderFactory implements RestClientBuilderFactory, InitializingBean, DisposableBean, AutoCloseable {
  /**
   * Default connection ttl setting. Should be less than server we're going to use.
   * Since this class is intended to be used mostly for interacting with brikar services, this TTL
   * setting should be set keeping in mind default 'keep alive' settings in brikar-server module.
   *
   * Since we're using jetty and default jetty's TTL is defined to 200000 - see _maxIdleTime in
   * org.eclipse.jetty.server.AbstractConnector class.
   *
   * We're picking slightly smaller value here to use jetty's resources in the most efficient way.
   */
  public static final long DEFAULT_CONNECTION_TTL = 60000L;

  /**
   * Default value for total maximum connections.
   */
  public static final int DEFAULT_MAX_CONN_TOTAL = 15;

  private final Logger log = LoggerFactory.getLogger(getClass());
  private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
  private RestServiceBinder restServiceBinder;
  private RestServiceBinderFactory restServiceBinderFactory;
  private final List<HttpMessageConverter<?>> messageConverters;
  private HttpComponentsClientHttpRequestFactory httpRequestFactory;
  private long connectionTtlMillis;
  private int maxConnTotal;
  private HttpRequestRetryHandler retryHandler;

  public StandardRestClientBuilderFactory(@Nonnull List<HttpMessageConverter<?>> messageConverters,
                                          @Nonnull RestServiceBinderFactory restServiceBinderFactory) {
    this.messageConverters = messageConverters;
    setRestServiceBinderFactory(restServiceBinderFactory);
    setConnectionTtlMillis(DEFAULT_CONNECTION_TTL);
    setMaxConnTotal(DEFAULT_MAX_CONN_TOTAL);
    setRetryHandler(null);
  }

  public StandardRestClientBuilderFactory(@Nonnull List<HttpMessageConverter<?>> messageConverters) {
    this(messageConverters, RestServiceBinderFactory.DEFAULT);
  }

  public StandardRestClientBuilderFactory(@Nonnull HttpMessageConverter<?>... messageConverters) {
    this(Arrays.asList(messageConverters));
  }

  /**
   * If set before bean is initialized, instructs to use the provided connection time to live value
   * that should be given in milliseconds.
   * If the provided value is negative, then no value will be set and default http client connection TTL settings will
   * be used which sets TTL to infitity.
   * <p>Should be set prior to {@link #afterPropertiesSet()}.</p>
   *
   * @param connectionTtlMillis Connection time to live, in milliseconds
   */
  public void setConnectionTtlMillis(long connectionTtlMillis) {
    this.connectionTtlMillis = connectionTtlMillis;
  }

  /**
   * If set before bean is initialized, instructs to use provided value as maximum connections.
   * <p>Should be set prior to {@link #afterPropertiesSet()}.</p>
   *
   * @param maxConnTotal Maximum total connections
   */
  public void setMaxConnTotal(int maxConnTotal) {
    this.maxConnTotal = maxConnTotal;
  }

  /**
   * Sets retry handler, that should be used for handling connectivity issues.
   * <p>Should be set prior to {@link #afterPropertiesSet()}.</p>
   *
   * @param retryHandler Retry handler
   */
  public void setRetryHandler(@Nullable HttpRequestRetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  /**
   * Sets service binder, that will create a service by given class and RestOperations instance.
   * <p>Should be set prior to {@link #afterPropertiesSet()}.</p>
   * <p>See also {@link RestServiceBinder}</p>
   *
   * @param factory Factory instance
   */
  public void setRestServiceBinderFactory(@Nonnull RestServiceBinderFactory factory) {
    this.restServiceBinderFactory = factory;
  }

  @Nonnull
  @Override
  public <T> RestClientBuilder<T> newClient(@Nonnull Class<T> serviceClass) {
    if (restServiceBinder == null) {
      throw new IllegalStateException("Bean has not been initialized properly");
    }
    return new InternalRestClientBuilder<>(serviceClass, restServiceBinder, credentialsProvider);
  }

  @Override
  public void afterPropertiesSet() {
    final HttpClientBuilder builder = HttpClientBuilder.create();
    initDefaultHttpClientBuilder(builder);

    httpRequestFactory = new HttpComponentsClientHttpRequestFactory(builder.build());
    final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
    initRestTemplate(restTemplate);

    restServiceBinder = restServiceBinderFactory.create(restTemplate);
  }

  @Override
  public void close() {
    if (httpRequestFactory != null) {
      try {
        httpRequestFactory.destroy();
      } catch (Exception e) {
        log.error("Error while shutting down httpRequestFactory", e);
      }
      httpRequestFactory = null;
    }
  }

  @Override
  public void destroy() {
    close();
  }

  //
  // Protected
  //

  protected void initTimings(@Nonnull HttpClientBuilder builder) {
    // copy settings
    final long connTtl = this.connectionTtlMillis;
    if (connTtl >= 0) {
      // don't store connection for more than given amount of milliseconds
      builder.setConnectionTimeToLive(connTtl, TimeUnit.MILLISECONDS);
    }
    builder.setMaxConnTotal(maxConnTotal);
  }

  protected void initCredentialsProvider(@Nonnull HttpClientBuilder builder) {
    // use builtin credentials provider
    builder.setDefaultCredentialsProvider(credentialsProvider);
  }

  protected void initRetryHandler(@Nonnull HttpClientBuilder builder) {
    HttpRequestRetryHandler retryHandler = this.retryHandler;
    if (retryHandler == null) {
      retryHandler = new DefaultHttpRequestRetryHandler(3, true);
    }

    builder.setRetryHandler(retryHandler);
  }

  protected void initRequestIdOperations(@Nonnull HttpClientBuilder builder) {
    builder.addInterceptorLast(new RequestLogAwareHttpRequestInterceptor());
    builder.addInterceptorLast(new RequestLogAwareHttpResponseInterceptor(log));
  }

  protected void initDefaultHttpClientBuilder(@Nonnull HttpClientBuilder builder) {
    initRequestIdOperations(builder);
    initTimings(builder);
    initCredentialsProvider(builder);
    initRetryHandler(builder);
  }

  protected void initRestTemplate(@Nonnull RestTemplate restTemplate) {
    restTemplate.setMessageConverters(messageConverters);
  }

  //
  // Private
  //

  private static final class InternalRestClientBuilder<T> implements RestClientBuilder<T> {
    private String username;
    private String password;
    private URI restServiceUri;
    private final RestServiceBinder restServiceBinder;
    private final CredentialsProvider credentialsProvider;
    private final Class<T> clazz;

    public InternalRestClientBuilder(@Nonnull Class<T> clazz,
                                     @Nonnull RestServiceBinder restServiceBinder,
                                     @Nonnull CredentialsProvider credentialsProvider) {
      this.clazz = clazz;
      this.restServiceBinder = restServiceBinder;
      this.credentialsProvider = credentialsProvider;
    }

    @Nonnull
    @Override
    public RestClientBuilder<T> setUri(@Nonnull URI uri) {
      this.restServiceUri = uri;
      return this;
    }

    @Nonnull
    @Override
    public RestClientBuilder<T> setUsername(@Nullable String username) {
      this.username = username;
      return this;
    }

    @Nonnull
    @Override
    public RestClientBuilder<T> setPassword(@Nullable String password) {
      this.password = password;
      return this;
    }

    @Nonnull
    @Override
    public T build() {
      final String username = this.username;
      final String password = this.password;
      final URI uri = this.restServiceUri;

      if (uri == null) {
        throw new BeanInitializationException("uri has not been set");
      }

      if (username != null && password != null) {
        credentialsProvider.setCredentials(new AuthScope(
                new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())),
            new UsernamePasswordCredentials(username, password));
      }

      final String url = restServiceUri.toString();
      return restServiceBinder.createClient(url, clazz);
    }
  }
}
