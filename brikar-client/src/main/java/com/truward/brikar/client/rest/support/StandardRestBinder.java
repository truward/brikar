package com.truward.brikar.client.rest.support;

import com.truward.brikar.client.binder.RestServiceBinder;
import com.truward.brikar.client.binder.RestServiceBinderFactory;
import com.truward.brikar.client.rest.RestBinder;
import com.truward.brikar.client.rest.RestClientBuilder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
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
 * Default implementation of {@link RestBinder}, adapted for spring framework.
 *
 * @author Alexander Shabanov
 */
public class StandardRestBinder implements RestBinder, InitializingBean, DisposableBean, AutoCloseable {
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

  private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
  private RestServiceBinder restServiceBinder;
  private RestServiceBinderFactory restServiceBinderFactory;
  private final List<HttpMessageConverter<?>> messageConverters;
  private HttpComponentsClientHttpRequestFactory httpRequestFactory;
  private long connectionTtlMillis;
  private HttpRequestRetryHandler retryHandler;

  public StandardRestBinder(@Nonnull List<HttpMessageConverter<?>> messageConverters,
                            @Nonnull RestServiceBinderFactory restServiceBinderFactory) {
    this.messageConverters = messageConverters;
    setRestServiceBinderFactory(restServiceBinderFactory);
    setConnectionTtlMillis(DEFAULT_CONNECTION_TTL);
    setRetryHandler(null);
  }

  public StandardRestBinder(@Nonnull List<HttpMessageConverter<?>> messageConverters) {
    this(messageConverters, RestServiceBinderFactory.DEFAULT);
  }

  public StandardRestBinder(@Nonnull HttpMessageConverter<?>... messageConverters) {
    this(Arrays.asList(messageConverters));
  }

  /**
   * If set before bean is initialized, instructs to use the provided connection time to live value
   * that should be given in milliseconds.
   * If the provided value is negative, then no value will be set and default http client connection TTL settings will
   * be used which sets TTL to infitity.
   * <p>Should be set prior to {@link #afterPropertiesSet()}.</p>
   *
   * @param connectionTtlMillis Connection time to live, in milliseconds.
   */
  public void setConnectionTtlMillis(long connectionTtlMillis) {
    this.connectionTtlMillis = connectionTtlMillis;
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
      httpRequestFactory.destroy();
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
    builder.setMaxConnTotal(100);
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

  protected void initDefaultHttpClientBuilder(@Nonnull HttpClientBuilder builder) {
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
