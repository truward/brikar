package com.truward.brikar.server.test.auth;

import com.truward.brikar.server.auth.SimpleAuthenticatorUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import org.junit.Test;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.StringReader;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SimpleAuthenticatorUtil}.
 *
 * @author Alexander Shabanov
 */
public final class SimpleAuthenticatorUtilTest {
  private static final String ONE_USER_PROPS = "myService.auth.users.1.username=alice\n" +
      "myService.auth.users.1.password=test\n" +
      "myService.auth.users.1.authorities=USER\n";

  private static final List<SimpleServiceUser> ONE_USER = singletonList(
      new SimpleServiceUser("alice", "test", singletonList(SimpleAuthenticatorUtil.USER_AUTHORITY)));

  @Test
  public void shouldLoadSingleUserEntry() throws IOException {
    // Given:
    @SuppressWarnings("UnnecessaryLocalVariable") final String content = ONE_USER_PROPS;

    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(
        createPropertySource(content), "myService.auth.users");

    // Then:
    assertEquals(ONE_USER, users);
  }

  @Test
  public void shouldLoadEmptyUserList() throws IOException {
    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(
        createPropertySource(""), "myService");

    // Then:
    assertTrue(users.isEmpty());
  }

  @Test
  public void shouldLoadMultipleEntries() throws IOException {
    // Given:
    final String content = "myService.auth.users.1.username=alice\n" +
        "myService.auth.users.1.password=test\n" +
        "myService.auth.users.1.authorities=ADMIN,USER\n" +
        "\n" +
        "myService.auth.users.213.username=bob\n" +
        "myService.auth.users.213.password=hello\n" +
        "myService.auth.users.213.authorities=\n" +
        "\n" +
        "myService.auth.users.2.username=dave\n" +
        "myService.auth.users.2.password=a\n";

    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(
        createPropertySource(content), "myService.auth.users")
        .stream()
        .sorted(Comparator.comparing(SimpleServiceUser::getUsername))
        .collect(Collectors.toList());

    // Then:
    assertEquals(asList(new SimpleServiceUser("alice", "test", asList("ADMIN", "USER")),
            new SimpleServiceUser("bob", "hello", SimpleAuthenticatorUtil.DEFAULT_AUTHORITIES),
            new SimpleServiceUser("dave", "a", SimpleAuthenticatorUtil.DEFAULT_AUTHORITIES)),
        users);
  }

  //
  // Private
  //

  @Nonnull
  public static PropertySource<?> createPropertySource(@Nonnull String content) throws IOException {
    final Properties properties = new Properties();
    properties.load(new StringReader(content));
    return new PropertiesPropertySource("testProperties", properties);
  }
}
