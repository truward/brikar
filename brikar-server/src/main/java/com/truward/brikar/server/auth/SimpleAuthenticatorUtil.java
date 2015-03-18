package com.truward.brikar.server.auth;

import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Credential;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
}
