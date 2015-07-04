package com.truward.brikar.test.exposure;

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

  public static void main(@Nonnull String[] args, @Nullable final ServerAware serverAware) throws Exception {
    final StandardLauncher launcher = new StandardLauncher("exposureTest") {
      @Override
      protected void configureLoggers() {
        // do nothing - special logging configuration is not required here
      }

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
        .start(args);
  }

//  public static void main(String[] args) throws Exception {
//    main(args, null);
//  }
}
