package com.truward.brikar.client.binder;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public interface RestServiceBinder {

  @Nonnull
  <T> T createClient(@Nonnull String serviceBaseUrl,
                     @Nonnull Class<T> restServiceClass,
                     @Nonnull Class<?> ... extraClasses);
}
