package com.truward.brikar.sample.calc.server;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * @author Alexander Shabanov
 */
public final class CalcLauncher {
  public static void main(String[] args) throws Exception {
    final StandardLauncher launcher = new StandardLauncher("classpath:/calcService/");
    launcher.start();
  }
}
