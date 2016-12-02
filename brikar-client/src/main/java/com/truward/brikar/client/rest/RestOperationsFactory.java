package com.truward.brikar.client.rest;

import com.truward.brikar.client.interceptor.RequestLogAwareHttpRequestInterceptor;
import com.truward.brikar.client.interceptor.RequestLogAwareHttpResponseInterceptor;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A helper class that pre configures RestOperations and underlying HTTP client to use the specified connection
 * settings.
 *
 * @author Alexander Shabanov
 */
public final class RestOperationsFactory implements DisposableBean, AutoCloseable {
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

  /**
   * Use logger with specified name - 'BrikarRestClient' that should be associated with all HTTP calls.
   */
  private final Logger log = LoggerFactory.getLogger("BrikarRestClient");

  private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
  private final List<HttpMessageConverter<?>> messageConverters;
  private HttpComponentsClientHttpRequestFactory httpRequestFactory;
  private long connectionTtlMillis;
  private int maxConnTotal;
  private HttpRequestRetryHandler retryHandler;

  public RestOperationsFactory(@Nonnull List<HttpMessageConverter<?>> messageConverters) {
    this.messageConverters = messageConverters;
    setConnectionTtlMillis(DEFAULT_CONNECTION_TTL);
    setMaxConnTotal(DEFAULT_MAX_CONN_TOTAL);
    setRetryHandler(null);
  }

  public RestOperationsFactory(@Nonnull HttpMessageConverter<?>... messageConverters) {
    this(Arrays.asList(messageConverters));
  }

  /**
   * If set before bean is initialized, instructs to use the provided connection time to live value
   * that should be given in milliseconds.
   * If the provided value is negative, then no value will be set and default http client connection TTL settings will
   * be used which sets TTL to infitity.
   * <p>Should be set prior to {@link #getRestOperations()} to take an effect.</p>
   *
   * @param connectionTtlMillis Connection time to live, in milliseconds
   */
  public void setConnectionTtlMillis(long connectionTtlMillis) {
    this.connectionTtlMillis = connectionTtlMillis;
  }

  /**
   * If set before bean is initialized, instructs to use provided value as maximum connections.
   * <p>Should be set prior to {@link #getRestOperations()} to take an effect.</p>
   *
   * @param maxConnTotal Maximum total connections
   */
  public void setMaxConnTotal(int maxConnTotal) {
    this.maxConnTotal = maxConnTotal;
  }

  /**
   * Sets retry handler, that should be used for handling connectivity issues.
   * <p>Should be set prior to {@link #getRestOperations()} to take an effect.</p>
   *
   * @param retryHandler Retry handler
   */
  public void setRetryHandler(@Nullable HttpRequestRetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  /**
   * Sets credentials for use when accessing service APIs.
   * Note, that multiple calls to this method override previously set credentials.
   *
   * @param credentials Credential list to use
   */
  public void setCredentials(@Nonnull List<ServiceClientCredentials> credentials) {
    credentialsProvider.clear();

    for (final ServiceClientCredentials cred : credentials) {
      final URI uri = cred.getBaseUri();
      credentialsProvider.setCredentials(new AuthScope(
              new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())),
          new UsernamePasswordCredentials(cred.getUsername(), cred.getPassword().toString()));
    }
  }

  /**
   * Returns rest operations object, suitable for use with the specified credentials
   *
   * @return New instance of {@link RestOperations} with all the connection settings provided prior to calling this
   */
  @Nonnull
  public RestOperations getRestOperations() {
    final HttpClientBuilder builder = HttpClientBuilder.create();
    initDefaultHttpClientBuilder(builder);

    httpRequestFactory = new HttpComponentsClientHttpRequestFactory(builder.build());
    final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
    initRestTemplate(restTemplate);
    return restTemplate;
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

  protected void initRequestVectorOperations(@Nonnull HttpClientBuilder builder) {
    builder.addInterceptorLast(new RequestLogAwareHttpRequestInterceptor());
    builder.addInterceptorLast(new RequestLogAwareHttpResponseInterceptor(log));
  }

  protected void initDefaultHttpClientBuilder(@Nonnull HttpClientBuilder builder) {
    initRequestVectorOperations(builder);
    initTimings(builder);
    initCredentialsProvider(builder);
    initRetryHandler(builder);
  }

  protected void initRestTemplate(@Nonnull RestTemplate restTemplate) {
    restTemplate.setMessageConverters(messageConverters);
  }
}
