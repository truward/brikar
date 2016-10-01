package com.truward.brikar.client.rest;

import com.truward.brikar.client.rest.support.StandardRestServiceBinder;
import org.springframework.web.client.RestOperations;

import javax.annotation.Nonnull;

/**
 * Factory class for {@link RestServiceBinder}
 *
 * @author Alexander Shabanov
 */
public interface RestServiceBinderFactory {
  /**
   * Default factory implementation.
   */
  RestServiceBinderFactory DEFAULT = new RestServiceBinderFactory() {
    @Nonnull
    @Override
    public RestServiceBinder create(@Nonnull RestOperations restOperations) {
      return new StandardRestServiceBinder(restOperations);
    }
  };

  @Nonnull
  RestServiceBinder create(@Nonnull RestOperations restOperations);
}
