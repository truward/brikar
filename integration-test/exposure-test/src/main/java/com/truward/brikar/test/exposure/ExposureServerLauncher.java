package com.truward.brikar.test.exposure;

import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.server.Server;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;


/**
 * @author Alexander Shabanov
 */
public final class ExposureServerLauncher {
  public static final String DEFAULT_DIR_PREFIX = "classpath:/exposureService/";

  public interface ServerAware {
    void setServer(@Nonnull Server server);
  }

  public static void main(@Nonnull List<String> configPaths,
                          @Nullable final ServerAware serverAware) throws Exception {
    final PropertySource<?> propertySource = StandardLauncher.createPropertySource(configPaths);
    final StandardLauncher launcher = new StandardLauncher(propertySource, DEFAULT_DIR_PREFIX) {
      @Override
      protected void setServerSettings(@Nonnull Server server) {
        super.setServerSettings(server);
        if (serverAware != null) {
          serverAware.setServer(server);
        }
      }
    };

    launcher
        .setSimpleSecurityEnabled(true)
        .setAuthPropertiesPrefix("exposureService.auth")
        .start();
  }
}
