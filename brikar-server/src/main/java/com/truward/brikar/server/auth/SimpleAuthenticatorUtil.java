package com.truward.brikar.server.auth;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;

/**
 * Utility class for working with embedded jetty authentication.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class SimpleAuthenticatorUtil {
  private SimpleAuthenticatorUtil() {}

  /**
   * Default role which is assigned in the absence of all other authorities.
   * Note, that Jetty basic authentication doesn't work properly with empty user collections.
   */
  public static final String USER_AUTHORITY = "USER";

  /**
   * Default authorities list which is assigned to the user if configuration specifies no authorities.
   */
  public static final List<String> DEFAULT_AUTHORITIES = Collections.singletonList(USER_AUTHORITY);

  private static final String DEFAULT_REALM = "default";

  private static final String DEFAULT_REALM_NAME = "defaultRealm";

  /**
   * Creates a handler for jetty server with the given list of users.
   * The list of users can be conveniently loaded by using the other method in
   * this utility class: {@link #loadUsers(PropertySource, String)}.
   *
   * @param users List of user entries
   * @return Jetty security handler
   */
  public static SecurityHandler newSecurityHandler(List<SimpleServiceUser> users) {
    final HashLoginService loginService = new HashLoginService();
    final Set<String> roles = new HashSet<>();
    for (final SimpleServiceUser user : users) {
      final List<String> authorities = user.getAuthorities();
      loginService.putUser(user.getUsername(), Credential.getCredential(user.getPassword()),
          authorities.toArray(new String[authorities.size()]));
      roles.addAll(authorities);

    }
    loginService.setName(DEFAULT_REALM);

    final Constraint constraint = new Constraint();
    constraint.setName(Constraint.__BASIC_AUTH);
    constraint.setRoles(roles.toArray(new String[roles.size()]));
    constraint.setAuthenticate(true);

    final ConstraintMapping constraintMapping = new ConstraintMapping();
    constraintMapping.setConstraint(constraint);
    constraintMapping.setPathSpec("/*");

    final ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
    csh.setAuthenticator(new BasicAuthenticator());
    csh.setRealmName(DEFAULT_REALM_NAME);
    csh.addConstraintMapping(constraintMapping);
    csh.setLoginService(loginService);

    return csh;
  }

  /**
   * Loads user records from a given reader.
   * The text stream produced by reader should contain records in java properties format (see also {@link Properties}).
   * Each property related to authorization entries should be prefixed. This prefix should be passed as
   * a second parameter to this method, {@code authPropertiesPrefix}.
   *
   * <p>
   * Example of property file:
   * <code>
   *   myService.auth.1.username=alice
   *   myService.auth.1.password=test
   *   myService.auth.1.roles=USER,ADMIN
   *
   *   myService.auth.1.username=bob
   *   myService.auth.1.password=password
   * </code>
   *
   * Sample call that would read {@code alice} and {@code bob} user entries from a sample property file given above:
   * <code>
   *   users = loadUsers(propertySource, "myService.auth");
   * </code>
   * </p>
   *
   * <p>
   * Note: if {@code .roles} property entry is missing for a user, the default value will be used, which is a list
   * of single element which is {@code USER}.
   * See also {@link SimpleServiceUser}.
   * </p>
   *
   * @param propertySource Property source, to read auth properties from
   * @param authPropertiesPrefix Prefix for properties in a given reader, that should be treated as user record entries
   * @return List of the parsed user entries
   */
  public static List<SimpleServiceUser> loadUsers(PropertySource<?> propertySource, String authPropertiesPrefix) {
    if (propertySource instanceof EnumerablePropertySource) {
      final EnumerablePropertySource<?> enumPropSource = (EnumerablePropertySource) propertySource;
      final String[] propertyNames = enumPropSource.getPropertyNames();

      final PropertyEntrySink sink = new PropertyEntrySink(authPropertiesPrefix);

      for (final String propertyName : propertyNames) {
        final Object value = propertySource.getProperty(propertyName);
        if (value instanceof String) {
          sink.putEntry(propertyName, value.toString());
        }
      }

      return sink.getUserList();
    }

    LoggerFactory.getLogger(SimpleAuthenticatorUtil.class)
        .warn("propertySource={} is not of type EnumerablePropertySource", propertySource);

    return Collections.emptyList();
  }

  //
  // Private
  //

  private static final class PropertyEntrySink {
    private final String authPropertiesPrefix;
    private final Map<String, MutableUserEntry> codeToMutableEntryMap = new HashMap<>();

    PropertyEntrySink(String authPropertiesPrefix) {
      this.authPropertiesPrefix = authPropertiesPrefix;
    }

    void putEntry(String key, String value) {
      if (!key.startsWith(authPropertiesPrefix)) {
        return;
      }

      if (key.length() < getExpectedEntryLength()) {
        getLogger().warn("Matched prefix {} in key {} but wrong length", authPropertiesPrefix, key); // unlikely
        return;
      }

      final int codeSep = key.indexOf('.', authPropertiesPrefix.length() + 1); // prefix + dot
      if (codeSep < 0) {
        getLogger().warn("Malformed entry {}={}", key, value); // unlikely
        return;
      }

      final String code = key.substring(authPropertiesPrefix.length() + 1, codeSep);
      final String fieldName = key.substring(codeSep + 1);

      MutableUserEntry e = codeToMutableEntryMap.get(code);
      if (e == null) {
        e = new MutableUserEntry();
        codeToMutableEntryMap.put(code, e);
      }

      switch (fieldName) {
        case "username":
          e.username = value;
          break;

        case "password":
          e.password = value;
          break;

        case "authorities":
          if (!value.isEmpty()) {
            e.authorities = Arrays.asList(value.split(","));
          }
          break;

        default:
          getLogger().warn("Malformed entry {}={}", key, value); // unlikely
      }
    }

    List<SimpleServiceUser> getUserList() {
      final List<SimpleServiceUser> result = new ArrayList<>();
      for (final MutableUserEntry e : codeToMutableEntryMap.values()) {
        result.add(new SimpleServiceUser(e.username, e.password, e.authorities));
      }
      return result;
    }

    //
    // Private
    //

    private Logger getLogger() {
      return LoggerFactory.getLogger(getClass());
    }

    private int getExpectedEntryLength() {
      return authPropertiesPrefix.length() + 4; // {prefix} + dot + code + dot + property name
    }

    private static final class MutableUserEntry {
      String username = "";
      String password = "";
      List<String> authorities = DEFAULT_AUTHORITIES;
    }
  } // class PropertyEntrySink
}
