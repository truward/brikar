package com.truward.brikar.server.util;

import org.eclipse.jetty.util.resource.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
public final class JettyResourceUtil {
  private JettyResourceUtil() {} // hidden

  public static Resource createResource(String resourcePath) throws IOException {
    if (resourcePath.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
      return Resource.newClassPathResource(resourcePath.substring(ResourceUtils.CLASSPATH_URL_PREFIX.length()));
    } else if (resourcePath.startsWith(ResourceUtils.FILE_URL_PREFIX)) {
      return Resource.newResource(new File(resourcePath.substring(ResourceUtils.FILE_URL_PREFIX.length())));
    } else if (resourcePath.startsWith("/")) {
      return Resource.newResource(new File(resourcePath));
    }

    throw new IOException("Unknown protocol in resourcePath=" + resourcePath);
  }
}
