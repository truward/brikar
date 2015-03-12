package com.truward.brikar.client.rest;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;

/**
 * Represents an abstraction for building REST clients.
 *
 * @param <T> Service interface
 *
 * @author Alexander Shabanov
 */
public interface RestClientBuilder<T> {

  @Nonnull
  RestClientBuilder<T> setUri(@Nonnull URI uri);

  @Nonnull
  RestClientBuilder<T> setUsername(@Nullable String username);

  @Nonnull
  RestClientBuilder<T> setPassword(@Nullable String password);

  @Nonnull
  T build();
}
