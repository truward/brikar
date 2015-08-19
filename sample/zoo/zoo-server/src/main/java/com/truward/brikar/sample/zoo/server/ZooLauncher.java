package com.truward.brikar.sample.zoo.server;

import com.truward.brikar.server.launcher.StandardLauncher;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class ZooLauncher extends StandardLauncher {
  public ZooLauncher(@Nonnull String defaultDirPrefix) {
    super(defaultDirPrefix);
  }

  public static void main(String[] args) throws Exception {
    new ZooLauncher("classpath:/zooService/").start().close();
  }

  @Override
  protected boolean isSpringSecurityEnabled() {
    return true;
  }
}
