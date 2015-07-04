package com.truward.brikar.test.exposure;

import com.truward.brikar.server.launcher.StandardLauncher;


/**
 * @author Alexander Shabanov
 */
public class ExposureServerLauncher {
  public static void main(String[] args) throws Exception {
//    final String runtimeName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
//    System.out.println("runtimeMX=" + runtimeName);

    new StandardLauncher("exposureTest")
        .setDefaultDirPrefix("classpath:/exposureService/")
        .setSimpleSecurityEnabled(true)
        .setAuthPropertiesPrefix("exposureService.auth")
        .start(args);
  }
}
