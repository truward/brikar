package com.truward.brikar.server.util;

import org.eclipse.jetty.util.resource.Resource;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    try {
      if (Files.exists(Paths.get(resourcePath))) {
        return Resource.newResource(new File(resourcePath));
      }

      // path seem to be valid but nothing exists there
      throw new IOException("Missing file at resourcePath=" + resourcePath);
    } catch (InvalidPathException e) {
      throw new IOException("Invalid path or unknown protocol in resourcePath=" + resourcePath, e);
    }
  }
}
