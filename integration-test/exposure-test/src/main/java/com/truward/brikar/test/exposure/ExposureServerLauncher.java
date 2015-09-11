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

  public static void main(@Nonnull List<String> extraConfigPaths,
                          @Nullable final ServerAware serverAware,
                          @Nonnull LaunchMode launchMode) throws Exception {
    StandardLauncher.ensureLoggersConfigured();

    final String dirPrefix = getConfigPath(launchMode);

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

    switch (launchMode) {
      case EXPOSURE_WITH_SIMPLE_SECURITY:
        launcher.setSimpleSecurityEnabled(true);
        launcher.setAuthPropertiesPrefix("exposureService.auth");
        break;
      case EXPOSURE_WITH_SPRING_SECURITY:
        launcher.setSpringSecurityEnabled(true);
        break;
      case STATIC_WEBSITE:
        launcher.setStaticHandlerEnabled(true);
        break;
      default:
        throw new IllegalArgumentException("Unknown launchMode=" + launchMode);
    }

    launcher.start();
    launcher.close();
  }

  @Nonnull
  private static String getConfigPath(@Nonnull LaunchMode launchMode) {
    switch (launchMode) {
      case EXPOSURE_WITH_SIMPLE_SECURITY:
        return "classpath:/exposureService/";
      case EXPOSURE_WITH_SPRING_SECURITY:
        return "classpath:/exposureServiceSpringSec/";
      case STATIC_WEBSITE:
        return "classpath:/staticWebsite/";
      default:
        throw new IllegalArgumentException("Unknown launchMode=" + launchMode);
    }
  }
}
