package com.truward.brikar.sample.rpcExplorerDemo;

import com.truward.brikar.rpc.servlet.RpcServiceServlet;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.annotation.Nonnull;

/**
 * @author Alexander Shabanov
 */
public final class Launcher {

  public static void main(String[] args) throws Exception {
    try (StandardLauncher launcher = new StandardLauncher("classpath:/rpcExplorerDemo/") {
      @Override
      protected void initServlets(@Nonnull ServletContextHandler contextHandler) {
        super.initServlets(contextHandler);

        final ServletHolder rpcServlet = contextHandler.addServlet(RpcServiceServlet.class,
            "/rpc/*");
        rpcServlet.setInitParameter("contextConfigLocation", getDispatcherServletConfigLocations());
      }
    }) {
      launcher.start();
    }
  }
}
