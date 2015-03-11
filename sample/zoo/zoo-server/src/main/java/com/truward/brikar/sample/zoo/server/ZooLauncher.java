package com.truward.brikar.sample.zoo.server;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * @author Alexander Shabanov
 */
public final class ZooLauncher {
  public static void main(String[] args) throws Exception {
    final StandardLauncher launcher = new StandardLauncher() {
      @Override
      protected boolean isSpringSecurityEnabled() {
        return true;
      }
    };

    launcher.start(args);
  }
}
