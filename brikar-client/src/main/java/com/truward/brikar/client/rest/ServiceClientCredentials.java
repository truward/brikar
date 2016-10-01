package com.truward.brikar.client.rest;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Objects;

/**
 * Credentials tuple for accessing service host, identified by {@link #baseUri}
 *
 * @author Alexander Shabanov
 */
public final class ServiceClientCredentials {
  private final URI baseUri;
  private final String username;
  private final CharSequence password;

  public ServiceClientCredentials(@Nonnull URI baseUri,
                                  @Nonnull String username,
                                  @Nonnull CharSequence password) {
    this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
    this.username = Objects.requireNonNull(username, "username");
    this.password = Objects.requireNonNull(password, "password");
  }

  @Nonnull
  public URI getBaseUri() {
    return baseUri;
  }

  @Nonnull
  public String getUsername() {
    return username;
  }

  @Nonnull
  public CharSequence getPassword() {
    return password;
  }

  @Override
  public String toString() {
    return String.format("ServiceClientCredentials{baseUri=%s, username=%s, password=#%d}",
        getBaseUri(), getUsername(), getPassword().hashCode());
  }
}
