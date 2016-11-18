package com.truward.brikar.maintenance;

import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.launcher.StandardLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for launching brikar processes.
 *
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public final class BrikarProcess implements AutoCloseable {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private final int port;
  private final Process process;
  private final String tempLogBaseName;
  private volatile boolean closed;

  private BrikarProcess(int port, Process process, @Nullable  String tempLogBaseName) {
    this.port = port;
    this.process = process;
    this.tempLogBaseName = tempLogBaseName;
  }

  public int getPort() {
    return port;
  }

  public Process getProcess() {
    return process;
  }

  public String getTempLogBaseName() {
    return tempLogBaseName;
  }

  @Nullable
  public File getActiveTempLog() {
    if (tempLogBaseName == null) {
      return null;
    }

    return new File(tempLogBaseName.concat(".log"));
  }

  public void waitUntilLaunched(@Nullable SimpleServiceUser user) {
    if (port <= 0 || port > 65535) {
      throw new IllegalStateException("Port is unknown, can't start");
    }

    ServerApiUtil.waitUntilStarted(user, URI.create(String.format("http://127.0.0.1:%d/api", getPort())));
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  @Override
  public void close() {
    if (closed) {
      return;
    }

    this.getProcess().destroy();

    if (tempLogBaseName == null) {
      return;
    }

    // delete temp log files
    final File prefix = new File(tempLogBaseName);
    try {
      Files.walkFileTree(prefix.getParentFile().toPath(), new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
          if (!file.toFile().getName().startsWith(prefix.getName())) {
            return FileVisitResult.CONTINUE;
          }

          final boolean deleted = file.toFile().delete();
          if (!deleted) {
            log.warn("Can't delete log file {}", file);
          }
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException ignored) {
    }

    closed = true;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static final class Builder {
    private String javaExecPath;
    private Map<String, String> defines = new HashMap<>();
    private URL[] classpathUrls;
    private String mainClassName;
    private String tempLoggerBaseName;
    private int tempConfigurationPort;

    private Builder() {
    }

    public Builder setJavaExecPath(@Nullable String value) {
      this.javaExecPath = value;
      return this;
    }

    public Builder addTempConfiguration(Map<String, String> properties) {
      final TempConfiguration tempConfiguration = new TempConfiguration(properties);

      if (!properties.containsKey(StandardLauncher.CONFIG_KEY_PORT)) {
        // no port - use random one
        final int port = LaunchUtil.getAvailablePort();
        tempConfiguration.setPort(port);
        this.tempConfigurationPort = port;
      }

      try {
        return addDefine(StandardLauncher.SYS_PROP_SETTINGS_OVERRIDE,
            tempConfiguration.writeToTempFile().toExternalForm());
      } catch (IOException e) {
        throw new IllegalStateException(e); // should never happen
      }
    }

    public Builder addTempLogger() {
      try {
        final File tempLog = File.createTempFile("brikar-templog-", "");
        tempLog.deleteOnExit();

        final String path = tempLog.getAbsolutePath();
        this.tempLoggerBaseName = path;
        addDefine("app.logback.logBaseName", path);
        addDefine("app.logback.rootLogId", "ROLLING_FILE");
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }

      return this;
    }

    public Builder addDefine(String key, String value) {
      defines.put(key, value);
      return this;
    }

    public Builder setClasspathUrls(URL... value) {
      this.classpathUrls = value;
      return this;
    }

    public Builder setMainClassName(String value) {
      this.mainClassName = value;
      return this;
    }

    public Builder setMainClass(Class<?> value) {
      return setMainClassName(value.getCanonicalName());
    }

    public BrikarProcess start() {
      final List<String> argList =  new ArrayList<>(5 + defines.size());

      final String mainClassName = this.mainClassName;
      if (!StringUtils.hasLength(mainClassName)) {
        throw new IllegalStateException("Main class has not been set");
      }

      String javaExecPath = this.javaExecPath;
      if (javaExecPath == null) {
        javaExecPath = LaunchUtil.getJavaExecPath();
      }

      argList.add(javaExecPath);

      for (final Map.Entry<String, String> define : defines.entrySet()) {
        argList.add("-D" + define.getKey() + '=' + define.getValue());
      }

      URL[] classpathUrls = this.classpathUrls;
      if (classpathUrls == null) {
        classpathUrls = LaunchUtil.getLocalClasspathUrls();
      }

      if (classpathUrls.length > 0) {
        argList.add("-classpath");
        argList.add(StringUtils.arrayToDelimitedString(classpathUrls, ":"));
      }

      argList.add(mainClassName);

      final Process process;
      try {
        final String[] args = argList.toArray(new String[argList.size()]);

        final String cmd = StringUtils.arrayToDelimitedString(args, " ");
        LoggerFactory.getLogger(getClass()).info("Starting {}", cmd);
        process = Runtime.getRuntime().exec(args);
      } catch (IOException e) {
        throw new IllegalStateException("Unable to start new java process", e);
      }

      return new BrikarProcess(tempConfigurationPort, process, tempLoggerBaseName);
    }
  }
}
