package com.truward.brikar.sample.rpcExplorerDemo.service;

import com.truward.brikar.sample.rpcExplorerDemo.model.UserModel;

import javax.annotation.Generated;

/**
 * TODO: remove, use UserModel.UserService
 *
 * @author Alexander Shabanov
 */
@Generated("")
public interface UserService {
  UserModel.GetUserReply getUser(UserModel.GetUserRequest request);

  UserModel.QueryUsersReply queryUsers(UserModel.QueryUsersRequest request);
}
