package com.truward.brikar.client.rest;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public interface RestClientBuilderFactory {

  @Nonnull
  <T> RestClientBuilder<T> newClient(@Nonnull Class<T> serviceClass);
}
