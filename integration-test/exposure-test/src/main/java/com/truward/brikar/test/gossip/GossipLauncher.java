package com.truward.brikar.test.gossip;

import com.truward.brikar.server.launcher.StandardLauncher;

/**
 * Launcher for gossip service.
 * <p>
 * This service is used to test OID propagation.
 * See also {@link com.truward.brikar.common.log.LogUtil#ORIGINATING_REQUEST_ID}.
 * </p>
 * <p>
 * Once launched with default setting, hit <code>http://127.0.0.1:8080/rest/gossip?about=something</code>, e.g.
 * <pre>curl http://127.0.0.1:8080/rest/gossip?about=something</pre> to see sample response.
 * </p>
 *
 * @author Alexander Shabanov
 */
public final class GossipLauncher {

  public static void main(String[] args) throws Exception {
    new StandardLauncher("classpath:/gossipService/").start().close();
  }
}
