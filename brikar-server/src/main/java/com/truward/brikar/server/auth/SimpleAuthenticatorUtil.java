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

import javax.annotation.Nonnull;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Utility class for working with embedded jetty authentication.
 *
 * @author Alexander Shabanov
 */
public final class SimpleAuthenticatorUtil {
  private SimpleAuthenticatorUtil() {}

  public static final String DEFAULT_REALM = "default";

  public static final String DEFAULT_REALM_NAME = "defaultRealm";

  @Nonnull
  public static SecurityHandler newSecurityHandler(@Nonnull List<SimpleServiceUser> users) {
    final HashLoginService loginService = new HashLoginService();
    final Set<String> roles = new HashSet<>();
    for (final SimpleServiceUser user : users) {
      final List<String> r = user.getRoles();
      loginService.putUser(user.getUsername(), Credential.getCredential(user.getPassword()),
          r.toArray(new String[r.size()]));
      roles.addAll(r);

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

  @Nonnull
  public static List<SimpleServiceUser> loadUsers(@Nonnull Reader reader,
                                                  @Nonnull String authEntryPrefix) throws IOException {
    final Properties properties = new Properties();
    properties.load(reader);

    final PropertyEntrySink sink = new PropertyEntrySink(authEntryPrefix);

    for (final Map.Entry<?, ?> entry : properties.entrySet()) {
      final Object key = entry.getKey();
      final Object value = entry.getValue();

      if (key instanceof String && value instanceof String) {
        sink.putEntry(key.toString(), value.toString());
      }
    }

    return sink.getUserList();
  }

  @Nonnull
  public static List<SimpleServiceUser> loadUsers(@Nonnull File file,
                                                  @Nonnull Charset charset,
                                                  @Nonnull String authEntryPrefix) throws IOException {
    try (final FileInputStream fs = new FileInputStream(file)) {
      try (final InputStreamReader reader = new InputStreamReader(fs, charset)) {
        return loadUsers(reader, authEntryPrefix);
      }
    }
  }

  @Nonnull
  public static List<SimpleServiceUser> loadUsers(@Nonnull File file,
                                                  @Nonnull String authEntryPrefix) throws IOException {
    return loadUsers(file, StandardCharsets.UTF_8, authEntryPrefix);
  }

  //
  // Private
  //

  private static final class PropertyEntrySink {
    private final String authEntryPrefix;
    private final Map<String, MutableUserEntry> codeToMutableEntryMap = new HashMap<>();

    public PropertyEntrySink(@Nonnull String authEntryPrefix) {
      this.authEntryPrefix = authEntryPrefix;
    }

    public void putEntry(@Nonnull String key, @Nonnull String value) {
      if (!key.startsWith(authEntryPrefix)) {
        return;
      }

      if (key.length() < getExpectedEntryLength()) {
        getLogger().warn("Matched prefix {} in key {} but wrong length", authEntryPrefix, key); // unlikely
        return;
      }

      final int codeSep = key.indexOf('.', authEntryPrefix.length() + 1); // prefix + dot
      if (codeSep < 0) {
        getLogger().warn("Malformed entry {}={}", key, value); // unlikely
        return;
      }

      final String code = key.substring(authEntryPrefix.length() + 1, codeSep);
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

        case "roles":
          if (value.isEmpty()) {
            // Special case: no roles at all
            e.roles = Collections.emptyList();
          } else {
            e.roles = Arrays.asList(value.split(","));
          }
          break;

        default:
          getLogger().warn("Malformed entry {}={}", key, value); // unlikely
      }
    }

    @Nonnull
    public List<SimpleServiceUser> getUserList() {
      final List<SimpleServiceUser> result = new ArrayList<>();
      for (final MutableUserEntry e : codeToMutableEntryMap.values()) {
        result.add(new SimpleServiceUser(e.username, e.password, e.roles));
      }
      return result;
    }

    //
    // Private
    //

    @Nonnull
    private Logger getLogger() {
      return LoggerFactory.getLogger(getClass());
    }

    private int getExpectedEntryLength() {
      return authEntryPrefix.length() + 4; // {prefix} + dot + code + dot + propertyname
    }

    private static final class MutableUserEntry {
      private String username = "";
      private String password = "";
      private List<String> roles = SimpleServiceUser.DEFAULT_ROLES;
    }
  }
}
