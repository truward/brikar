package com.truward.brikar.test.exposure;

import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.server.Server;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * @author Alexander Shabanov
 */
public final class ExposureServerLauncher {

  public interface ServerAware {
    void setServer(@Nonnull Server server);
  }

  public static void main(@Nonnull List<String> extraConfigPaths,
                          @Nullable final ServerAware serverAware,
                          final boolean springSecurityEnabled) throws Exception {
    StandardLauncher.ensureLoggersConfigured();

    final String dirPrefix = springSecurityEnabled ? "classpath:/exposureServiceSpringSec/" :
        "classpath:/exposureService/";

    final List<String> configPaths = new ArrayList<>(StandardLauncher.getConfigurationPaths(dirPrefix));
    configPaths.addAll(extraConfigPaths);

    final PropertySource<?> propertySource = StandardLauncher.createPropertySource(configPaths);

    final StandardLauncher launcher = new StandardLauncher(new Callable<PropertySource<?>>() {
      @Override
      public PropertySource<?> call() throws Exception {
        return propertySource;
      }
    }, dirPrefix) {
      @Override
      protected void setServerSettings(@Nonnull Server server) {
        super.setServerSettings(server);
        if (serverAware != null) {
          serverAware.setServer(server);
        }
      }
    };

    if (!springSecurityEnabled) {
      launcher.setSimpleSecurityEnabled(true);
      launcher.setAuthPropertiesPrefix("exposureService.auth");
    } else {
      launcher.setSpringSecurityEnabled(true);
    }

    launcher.start();
    launcher.close();
  }
}
