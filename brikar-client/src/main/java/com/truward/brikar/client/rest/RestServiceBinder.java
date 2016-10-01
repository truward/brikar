package com.truward.brikar.client.rest;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * @author Alexander Shabanov
 */
public interface RestServiceBinder {

  @Nonnull
  <T> T createClient(@Nonnull String serviceBaseUrl,
                     @Nonnull Class<T> restServiceClass,
                     @Nonnull Class<?> ... extraClasses);

  @Nonnull
  <T> T createClient(@Nonnull URI serviceBaseUrl,
                     @Nonnull Class<T> restServiceClass,
                     @Nonnull Class<?> ... extraClasses);
}
