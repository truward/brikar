package com.truward.brikar.sample.zoo.server;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * @author Alexander Shabanov
 */
public final class ZooLauncher extends StandardLauncher {
  public static void main(String[] args) throws Exception {
    new ZooLauncher().setDefaultDirPrefix("classpath:/zooService/").start(args);
  }

  @Override
  protected boolean isSpringSecurityEnabled() {
    return true;
  }
}
