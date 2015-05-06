package com.truward.brikar.server.test.auth;

import com.truward.brikar.server.auth.SimpleAuthenticatorUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Alexander Shabanov
 */
public final class SimpleAuthenticatorUtilTest {
  private static final String ONE_USER_PROPS = "myService.auth.users.1.username=alice\n" +
      "myService.auth.users.1.password=test\n" +
      "myService.auth.users.1.roles=ROLE_USER\n";

  private static final List<SimpleServiceUser> ONE_USER = singletonList(
      new SimpleServiceUser("alice", "test", singletonList("ROLE_USER")));

  @Test
  public void shouldLoadSingleUserEntry() throws IOException {
    // Given:
    final String content = ONE_USER_PROPS;

    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(
        new StringReader(content), "myService.auth.users");

    // Then:
    assertEquals(ONE_USER, users);
  }

  @Test
  public void shouldLoadEmptyUserList() throws IOException {
    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(
        new StringReader(""), "myService");

    // Then:
    assertTrue(users.isEmpty());
  }

  @Test
  public void shouldLoadMultipleEntries() throws IOException {
    // Given:
    final String content = "myService.auth.users.1.username=alice\n" +
        "myService.auth.users.1.password=test\n" +
        "myService.auth.users.1.roles=ROLE_ADMIN,ROLE_USER\n" +
        "\n" +
        "myService.auth.users.213.username=bob\n" +
        "myService.auth.users.213.password=hello\n" +
        "myService.auth.users.213.roles=\n" +
        "\n" +
        "myService.auth.users.2.username=dave\n" +
        "myService.auth.users.2.password=a\n";

    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(
        new StringReader(content), "myService.auth.users");

    // Then:
    assertEquals(new HashSet<>(asList(new SimpleServiceUser("alice", "test", asList("ROLE_ADMIN", "ROLE_USER")),
            new SimpleServiceUser("bob", "hello", Collections.<String>emptyList()),
            new SimpleServiceUser("dave", "a", SimpleServiceUser.DEFAULT_ROLES))),
        new HashSet<>(users));
  }

  @Test
  public void shouldReadPropertiesFromFile() throws IOException {
    // Given:
    final File temp = File.createTempFile("shouldReadPropertiesFromFile", ".properties");
    temp.deleteOnExit();

    try (final FileOutputStream fs = new FileOutputStream(temp)) {
      fs.write(ONE_USER_PROPS.getBytes(StandardCharsets.UTF_8));
    }

    // When:
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(temp, "myService.auth.users");

    // Then:
    assertEquals(ONE_USER, users);
  }
}
