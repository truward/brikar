package com.truward.brikar.sample.rpcExplorerDemo.service;

import com.google.protobuf.StringValue;
import com.truward.brikar.sample.rpcExplorerDemo.model.UserModel;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Interface to UserService
 */
@ParametersAreNonnullByDefault
public interface UserService {
  UserModel.GetUserReply getUser(UserModel.GetUserRequest request);

  UserModel.QueryUsersReply queryUsers(UserModel.QueryUsersRequest request);

  StringValue triggerOutOfMemoryError(StringValue request);
}
