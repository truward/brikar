package com.truward.brikar.sample.todo.server;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * @author Alexander Shabanov
 */
public final class TodoLauncher {
  public static void main(String[] args) throws Exception {
    new StandardLauncher("classpath:/todoService/")
        .setSimpleSecurityEnabled(true)
        .setAuthPropertiesPrefix("todoService.auth")
        .start()
        .close();
  }
}
