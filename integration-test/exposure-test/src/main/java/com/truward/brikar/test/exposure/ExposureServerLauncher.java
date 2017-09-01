package com.truward.brikar.test.exposure;

import com.truward.brikar.server.launcher.StandardLauncher;
import com.truward.brikar.test.exposure.rpcV2.RpcV2Launcher;
import org.eclipse.jetty.server.Server;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Alexander Shabanov
 */
public final class ExposureServerLauncher {

  public static void main(@Nonnull List<String> extraConfigPaths,
                          @Nullable final ServerAware serverAware,
                          @Nonnull LaunchMode launchMode) throws Exception {
    StandardLauncher.ensureLoggersConfigured();

    if (launchMode == LaunchMode.RPC_SERVICE_V2) {
      RpcV2Launcher.main(extraConfigPaths, serverAware);
      return;
    }

    final String dirPrefix = getConfigPath(launchMode);

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
    };

    switch (launchMode) {
      case EXPOSURE_WITH_SIMPLE_SECURITY:
        launcher.setSimpleSecurityEnabled(true);
        launcher.setAuthPropertiesPrefix("exposureService.auth");
        break;
      case EXPOSURE_WITH_SPRING_SECURITY:
        launcher.setSpringSecurityEnabled(true);
        break;
      case RPC_SERVICE:
        // do nothing
        break;
      case STATIC_WEBSITE:
      case STATIC_WEBSITE_CUSTOM_PATH:
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
      case RPC_SERVICE:
        return "classpath:/rpcService/";
      case STATIC_WEBSITE:
        return "classpath:/staticWebsite/";
      case STATIC_WEBSITE_CUSTOM_PATH:
        return "classpath:/staticWebsiteCustomPath/";
      default:
        throw new IllegalArgumentException("Unknown launchMode=" + launchMode);
    }
  }
}
