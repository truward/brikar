package com.truward.brikar.test.exposure;

import com.truward.brikar.server.launcher.LauncherProperties;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.eclipse.jetty.server.Server;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


/**
 * @author Alexander Shabanov
 */
public class ExposureServerLauncher {
  public interface ServerAware {
    void setServer(@Nonnull Server server);
  }

  public static void main(@Nonnull LauncherProperties properties,
                          @Nullable final ServerAware serverAware) throws Exception {
    final StandardLauncher launcher = new StandardLauncher(properties, "exposureTest") {
      @Override
      protected void setServerSettings(@Nonnull Server server) {
        super.setServerSettings(server);
        if (serverAware != null) {
          serverAware.setServer(server);
        }
      }
    };

    launcher
        .setDefaultDirPrefix("classpath:/exposureService/")
        .setSimpleSecurityEnabled(true)
        .setAuthPropertiesPrefix("exposureService.auth")
        .start();
  }
}
