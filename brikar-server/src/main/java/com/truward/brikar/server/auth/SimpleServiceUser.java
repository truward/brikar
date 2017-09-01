package com.truward.brikar.server.auth;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a simple user account data. Password is not encrypted and stored in the clear text.
 *
 * @author Alexander Shabanov
 */
public final class SimpleServiceUser {
  private final String username;
  private final String password;
  private final List<String> authorities;

  public SimpleServiceUser(@Nonnull String username, @Nonnull String password, @Nonnull List<String> authorities) {
    this.username = Objects.requireNonNull(username, "username");
    this.password = Objects.requireNonNull(password, "password");

    Objects.requireNonNull(authorities, "authorities");
    this.authorities = unmodifiableList(asList(authorities.toArray(new String[authorities.size()])));
  }

  public SimpleServiceUser(@Nonnull String username, @Nonnull String password) {
    this(username, password, Collections.emptyList());
  }

  @Nonnull
  public String getUsername() {
    return username;
  }

  @Nonnull
  public String getPassword() {
    return password;
  }

  @Nonnull
  public List<String> getAuthorities() {
    return authorities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimpleServiceUser that = (SimpleServiceUser) o;

    return password.equals(that.password) && authorities.equals(that.authorities) && username.equals(that.username);
  }

  @Override
  public int hashCode() {
    int result = username.hashCode();
    result = 31 * result + password.hashCode();
    result = 31 * result + authorities.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SimpleServiceUser{" +
        "username='" + getUsername() + '\'' +
        ", passwordHash=" + getPassword().hashCode() +
        ", authorities=" + getAuthorities() +
        '}';
  }
}
