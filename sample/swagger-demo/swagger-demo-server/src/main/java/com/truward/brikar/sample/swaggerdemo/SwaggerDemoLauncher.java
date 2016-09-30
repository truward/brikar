package com.truward.brikar.sample.swaggerdemo;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * @author Alexander Shabanov
 */
public final class SwaggerDemoLauncher {
  public static void main(String[] args) throws Exception {
    new StandardLauncher("classpath:/swaggerDemo/").start().close();
  }
}
