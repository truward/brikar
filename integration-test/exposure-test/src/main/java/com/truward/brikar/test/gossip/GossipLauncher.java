package com.truward.brikar.test.gossip;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * Launcher for gossip service.
 * <p>
 * This service is used to test request ID propagation.
 * </p>
 * <p>
 * Once launched with default setting, hit <code>http://127.0.0.1:8080/api/gossip?about=something</code>, e.g.
 * <pre>curl http://127.0.0.1:8080/api/gossip?about=something</pre> to see sample response.
 * </p>
 *
 * @author Alexander Shabanov
 */
public final class GossipLauncher {

  public static void main(String[] args) throws Exception {
    launch("classpath:/gossipService/");
  }

  public static void launch(String configPath) throws Exception {
    try (final StandardLauncher launcher  = new StandardLauncher(configPath)) {
      launcher.start();
    }
  }
}
