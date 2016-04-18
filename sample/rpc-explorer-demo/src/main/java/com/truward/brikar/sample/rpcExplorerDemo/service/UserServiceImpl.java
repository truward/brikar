package com.truward.brikar.sample.rpcExplorerDemo.service;

import com.truward.brikar.sample.rpcExplorerDemo.model.UserModel;

/**
 * Implementation of UserService remote interface.
 *
 * @author Alexander Shabanov
 */
public final class UserServiceImpl implements UserService {

  @Override
  public UserModel.GetUserReply getUser(UserModel.GetUserRequest request) {
    final long userId = request.getUserId();
    final UserModel.GetUserReply.Builder replyBuilder = UserModel.GetUserReply.newBuilder();
    if (userId == 10L) {
      replyBuilder.setUser(UserModel.User.newBuilder()
          .setId(userId)
          .setName("Sample User")
          .setPassword("Password")
          .build());
    }
    return replyBuilder.build();
  }
}
