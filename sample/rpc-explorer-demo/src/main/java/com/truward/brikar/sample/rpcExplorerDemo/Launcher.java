package com.truward.brikar.sample.rpcExplorerDemo;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * @author Alexander Shabanov
 */
public final class Launcher {

  public static void main(String[] args) throws Exception {
    try (StandardLauncher launcher = new StandardLauncher("classpath:/rpcExplorerDemo/")) {
      launcher.start();
    }
  }
}
