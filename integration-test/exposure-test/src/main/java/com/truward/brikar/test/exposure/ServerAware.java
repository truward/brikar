package com.truward.brikar.test.exposure;

import org.eclipse.jetty.server.Server;

import javax.annotation.Nonnull;

/**
 * Callback that accepts server instance once it becomes available.
 *
 * @author Alexander Shabanov
 */
public interface ServerAware {
  void setServer(@Nonnull Server server);
}
