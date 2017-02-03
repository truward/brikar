package com.truward.brikar.test.exposure.rpcV2;

import com.truward.brikar.rpc.servlet.RpcServiceServlet;
import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.brikar.test.exposure.ServerAware;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class RpcV2Launcher {

  public static void main(@Nonnull List<String> extraConfigPaths,
                          @Nullable final ServerAware serverAware) throws Exception {

    final String dirPrefix = "classpath:/rpcServiceV2/";

    final List<String> configPaths = new ArrayList<>(StandardLauncher.getConfigurationPaths(dirPrefix));
    configPaths.addAll(extraConfigPaths);

    final PropertySource<?> propertySource = StandardLauncher.createPropertySource(configPaths);

    final StandardLauncher launcher = new StandardLauncher(() -> propertySource, dirPrefix) {
      @Override
      protected void setServerSettings(@Nonnull Server server) {
        super.setServerSettings(server);
        if (serverAware != null) {
          serverAware.setServer(server);
        }
      }

      @Override
      protected void initServlets(@Nonnull ServletContextHandler contextHandler) {
        super.initServlets(contextHandler);

        final ServletHolder rpcServlet = contextHandler.addServlet(RpcServiceServlet.class,
            "/rpc/*");
        rpcServlet.setInitParameter("contextConfigLocation", getDispatcherServletConfigLocations());
      }
    };

    launcher.start();
    launcher.close();
  }
}
