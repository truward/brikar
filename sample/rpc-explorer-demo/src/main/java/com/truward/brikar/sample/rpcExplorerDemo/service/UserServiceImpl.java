package com.truward.brikar.sample.rpcExplorerDemo.service;

import com.google.protobuf.StringValue;
import com.truward.brikar.sample.rpcExplorerDemo.model.UserModel;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Implementation of UserService remote interface.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class UserServiceImpl implements UserService {

  @Override
  public UserModel.GetUserReply getUser(UserModel.GetUserRequest request) {
    final long userId = request.getUserId();
    if (userId <= 0) {
      throw new IllegalArgumentException("userId=" + userId);
    }

    final UserModel.GetUserReply.Builder replyBuilder = UserModel.GetUserReply.newBuilder();
    if (userId == 10L) {
      replyBuilder.setUser(UserModel.User.newBuilder()
          .setId(userId)
          .setName("Sample User")
          .setPassword("Password")
          .addRoles("ROLE_USER")
          .build());
    }
    return replyBuilder.build();
  }

  @Override
  public UserModel.QueryUsersReply queryUsers(UserModel.QueryUsersRequest request) {
    return UserModel.QueryUsersReply.newBuilder()
        .addAllUsers(request.getUserIdsList()
            .stream()
            .map(id -> UserModel.User.newBuilder()
                .setId(id)
                .setName("User#" + id)
                .setPassword("Password")
                .addAllRoles(getRolesForId(id))
                .build())
            .collect(Collectors.toList()))
        .build();
  }

  @Override
  public StringValue triggerOutOfMemoryError(StringValue request) {
    final char[][] chars = new char[Integer.MAX_VALUE][Integer.MAX_VALUE];
    // should not come here (OOM should be triggered on the level above)
    final int randPos = ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE);
    Arrays.fill(chars[randPos], 'a');
    return StringValue.newBuilder()
        .setValue(new String(chars[ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE)]))
        .build();
  }

  //
  // Private
  //

  private static List<String> getRolesForId(long id) {
    if (id % 3 == 0) {
      return Arrays.asList("ROLE_USER", "ROLE_AUTHOR");
    } else if (id % 7 == 0) {
      return Arrays.asList("ROLE_USER", "ROLE_AUTHOR", "ROLE_ADMIN");
    }

    return Collections.singletonList("ROLE_USER");
  }
}
