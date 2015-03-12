package com.truward.brikar.client.rest.support;

import com.truward.brikar.client.binder.RestServiceBinder;
import com.truward.brikar.client.binder.support.StandardRestServiceBinder;
import com.truward.brikar.client.rest.RestBinder;
import com.truward.brikar.client.rest.RestClientBuilder;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class StandardRestBinder implements RestBinder, InitializingBean, DisposableBean, AutoCloseable {
  private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
  private RestServiceBinder restServiceBinder;
  private final List<HttpMessageConverter<?>> messageConverters;
  private HttpComponentsClientHttpRequestFactory httpRequestFactory;

  public StandardRestBinder(@Nonnull List<HttpMessageConverter<?>> messageConverters) {
    this.messageConverters = messageConverters;
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
    initHttpClientBuilder(builder);

    httpRequestFactory = new HttpComponentsClientHttpRequestFactory(builder.build());
    final RestTemplate restTemplate = new RestTemplate(httpRequestFactory);
    initRestTemplate(restTemplate);

    this.restServiceBinder = new StandardRestServiceBinder(restTemplate);
  }

  @Override
  public void close() {
    if (httpRequestFactory != null) {
      httpRequestFactory.destroy();
      httpRequestFactory = null;
    }
  }

  @Override
  public void destroy() throws Exception {
    close();
  }

  //
  // Protected
  //

  protected void initHttpClientBuilder(@Nonnull HttpClientBuilder httpClientBuilder) {
    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
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
        this.credentialsProvider.setCredentials(new AuthScope(
                new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme())),
            new UsernamePasswordCredentials(username, password));
      }

      final String url = restServiceUri.toString();
      return restServiceBinder.createClient(url, clazz);
    }
  }
}
