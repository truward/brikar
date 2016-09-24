package com.truward.brikar.server.auth;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a simple user account data. Password is not encrypted and stored in the clear text.
 *
 * @author Alexander Shabanov
 */
public final class SimpleServiceUser {
  public static final String ROLE_USER = "ROLE_USER";
  public static final List<String> DEFAULT_ROLES = unmodifiableList(singletonList(ROLE_USER));

  private final String username;
  private final String password;
  private final List<String> roles;

  public SimpleServiceUser(@Nonnull String username, @Nonnull String password, @Nonnull List<String> roles) {
    this.username = Objects.requireNonNull(username, "username");
    this.password = Objects.requireNonNull(password, "password");

    Objects.requireNonNull(roles, "roles");
    this.roles = unmodifiableList(asList(roles.toArray(new String[roles.size()])));
  }

  public SimpleServiceUser(@Nonnull String username, @Nonnull String password) {
    this(username, password, DEFAULT_ROLES);
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
  public List<String> getRoles() {
    return roles;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    SimpleServiceUser that = (SimpleServiceUser) o;

    return password.equals(that.password) && roles.equals(that.roles) && username.equals(that.username);
  }

  @Override
  public int hashCode() {
    int result = username.hashCode();
    result = 31 * result + password.hashCode();
    result = 31 * result + roles.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "SimpleServiceUser{" +
        "username='" + getUsername() + '\'' +
        ", passwordHash=" + getPassword().hashCode() +
        ", roles=" + getRoles() +
        '}';
  }
}
