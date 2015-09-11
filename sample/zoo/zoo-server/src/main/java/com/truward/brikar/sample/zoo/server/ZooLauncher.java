package com.truward.brikar.sample.zoo.server;

import com.truward.brikar.server.launcher.StandardLauncher;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class ZooLauncher {
  public static void main(String[] args) throws Exception {
    new StandardLauncher("classpath:/zooService/").setSpringSecurityEnabled(true).start().close();
  }
}
