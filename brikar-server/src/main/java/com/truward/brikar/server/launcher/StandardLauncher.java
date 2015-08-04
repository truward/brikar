package com.truward.brikar.server.launcher;

import com.truward.brikar.server.auth.SimpleAuthenticatorUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.tracking.RequestIdAwareFilter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.DispatcherType;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.*;

/**
 * @author Alexander Shabanov
 */
public class StandardLauncher {
  private final LauncherProperties properties;
  private ServletContextHandler contextHandler;
  private String defaultDirPrefix;
  private boolean simpleSecurityEnabled;
  private boolean requestIdOperationsEnabled;
  private String simpleSecurityOverridePath;
  private String authPropertiesPrefix = "auth";
  private String configPath;

  public StandardLauncher(@Nonnull LauncherProperties properties, @Nonnull String defaultDirPrefix) {
    this.properties = properties;
    // Loggers need to be configured as soon as possible, otherwise jetty will use its own default logger
    configureLoggers();

    setRequestIdOperationsEnabled(true);
    setSimpleSecurityOverridePath(null);
    setDefaultDirPrefix(defaultDirPrefix);
  }

  public StandardLauncher(@Nonnull String defaultDirPrefix) {
    this(DefaultLauncherProperties.createWithSystemProperties(), defaultDirPrefix);
  }

  public StandardLauncher() {
    this("classpath:/");
  }

  @Nonnull
  public StandardLauncher setSimpleSecurityEnabled(boolean enabled) {
    this.simpleSecurityEnabled = enabled;
    return this;
  }

  @Nonnull
  public StandardLauncher setRequestIdOperationsEnabled(boolean enabled) {
    this.requestIdOperationsEnabled = enabled;
    return this;
  }

  @Nonnull
  public StandardLauncher setSimpleSecurityOverridePath(@Nullable String simpleSecurityOverridePath) {
    this.simpleSecurityOverridePath = simpleSecurityOverridePath;
    return this;
  }

  @Nonnull
  public StandardLauncher setAuthPropertiesPrefix(@Nonnull String authPropertiesPrefix) {
    this.authPropertiesPrefix = authPropertiesPrefix;
    return this;
  }

  public final void start() throws Exception {
    startServer();
  }

  @Nonnull
  public StandardLauncher setDefaultDirPrefix(@Nonnull String defaultDirPrefix) {
    Assert.notNull(defaultDirPrefix, "defaultDirPrefix can't be null");
    this.defaultDirPrefix = defaultDirPrefix;
    return this;
  }

  @Nonnull
  public String getConfigPath() {
    final String configPath = this.configPath;
    if (configPath == null) {
      return getDefaultConfigPath();
    }
    return configPath;
  }


  //
  // Protected
  //

  @Nonnull
  protected String getDefaultConfigPath() {
    return defaultDirPrefix + "default.properties";
  }

  @Nonnull
  protected Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  protected void configureLoggers() {
    // initialize logback if logback.configurationFile property has not been set
    if (System.getProperty("logback.configurationFile") == null) {
      System.setProperty("logback.configurationFile", "default-service-logback.xml");
    }
  }

  @Nonnull
  protected List<Handler> getHandlers() {
    return Collections.<Handler>singletonList(contextHandler);
  }

  @Nonnull
  protected String getSpringContextLocations() {
    return defaultDirPrefix + "spring/service.xml";
  }

  @Nonnull
  protected String getDispatcherServletConfigLocations() {
    return defaultDirPrefix + "spring/webmvc.xml";
  }

  protected boolean isSpringSecurityEnabled() {
    return false;
  }

  protected void initContextFilters(@Nonnull ServletContextHandler contextHandler) {
    // Enforce UTF-8 encoding -
    // this should be done on the LB side for browsers and this doesn't needed to be done for protobuf API
    //
    // enable this if and only if UTF-8 needs to be unconditionally appended to the Content-Type
    // this is usually not needed

//    final FilterHolder encFilterHolder = contextHandler.addFilter(CharacterEncodingFilter.class,
//        "/*", EnumSet.allOf(DispatcherType.class));
//    encFilterHolder.setInitParameter("encoding", "UTF-8");
//    encFilterHolder.setInitParameter("forceEncoding", "true"); // <-- this line instructs filter to add encoding

    if (isSpringSecurityEnabled()) {
      initSpringSecurity(contextHandler);
    }

    if (requestIdOperationsEnabled) {
      initRequestIdOperations(contextHandler);
    }
  }

  protected void initSpringSecurity(@Nonnull ServletContextHandler contextHandler) {
    final FilterHolder holder = new FilterHolder(DelegatingFilterProxy.class);
    holder.setName("springSecurityFilterChain");
    contextHandler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
  }

  protected void initRequestIdOperations(@Nonnull ServletContextHandler contextHandler) {
    final FilterHolder holder = new FilterHolder(RequestIdAwareFilter.class);
    holder.setName("requestIdAwareFilter");
    contextHandler.addFilter(holder, "/*", EnumSet.allOf(DispatcherType.class));
  }

  protected void initServlets(@Nonnull ServletContextHandler contextHandler) {
    final ServletHolder dispatcherServlet = contextHandler.addServlet(DispatcherServlet.class,
        "/g/*,/rest/*,/j_spring_security_check");
    dispatcherServlet.setInitParameter("contextConfigLocation", getDispatcherServletConfigLocations());

    if (simpleSecurityEnabled) {
      initSimpleSecurity(contextHandler);
    }
  }

  protected void initSimpleSecurity(@Nonnull ServletContextHandler contextHandler) {
    try {
      contextHandler.setSecurityHandler(SimpleAuthenticatorUtil.newSecurityHandler(getSimpleServiceUsers()));
    } catch (IOException e) {
      throw new IllegalStateException("Can't initialize simple security", e);
    }
  }

  @Nonnull
  protected List<SimpleServiceUser> getSimpleServiceUsers() throws IOException {
    final ResourceLoader loader = new DefaultResourceLoader();
    for (final String path : getSimpleServiceResourcePaths()) {
      getLogger().info("Simple security: trying to load users from {}", path);

      final Resource resource = loader.getResource(path);
      if (!resource.exists()) {
        getLogger().info("Simple security: missing configuration at {}", path);
        continue;
      }

      try (final InputStream is = resource.getInputStream()) {
        return SimpleAuthenticatorUtil.loadUsers(is, SimpleAuthenticatorUtil.DEFAULT_CHARSET, authPropertiesPrefix);
      }
    }

    // fallback: there is no settings, use default username and generate password on the fly
    // normally this is not something service owner should rely on
    final Random random = new SecureRandom();
    final String username = "serviceuser";
    final String password = Long.toHexString(random.nextLong());
    getLogger().warn("Simple security: no predefined configuration; using username={} and password={}", username, password);
    return Collections.singletonList(new SimpleServiceUser(username, password));
  }

  @Nonnull
  protected List<String> getSimpleServiceResourcePaths() {
    final List<String> result = new ArrayList<>(3);

    // Explicit path override
    if (simpleSecurityOverridePath != null) {
      result.add(simpleSecurityOverridePath);
    }

    // Property override for simple security
    final String path = properties.getSimpleSecuritySettingsFilePath();
    if (path != null) {
      result.add(path);
    }

    // Default configuration path
    result.add(getConfigPath());

    return result;
  }

  protected void startServer() throws Exception {
    this.configPath = properties.getConfigPath() != null ? properties.getConfigPath() : getDefaultConfigPath();
    // set this property, so that spring security will be able to read it
    System.setProperty(DefaultLauncherProperties.SYS_PROP_NAME_CONFIG_PATH, configPath);

    final Server server = new Server(properties.getPort());
    setServerSettings(server);

    contextHandler = new ServletContextHandler(getServletContextOptions());
    contextHandler.setContextPath("/");
    initSpringContext();

    final HandlerCollection handlerList = new HandlerCollection();
    final List<Handler> handlers = getHandlers();
    handlerList.setHandlers(handlers.toArray(new Handler[handlers.size()]));
    server.setHandler(handlerList);

    setShutdownStrategy(server);

    server.start();
    server.join();
  }

  protected int getServletContextOptions() {
    //noinspection PointlessBitwiseExpression
    return ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY;
  }

  protected void setServerSettings(@Nonnull Server server) {
    server.setSendServerVersion(false);
  }

  protected void setShutdownStrategy(@Nonnull Server server) {
    // stop receiving connections after given amount of milliseconds
    server.setGracefulShutdown(properties.getGracefulShutdownMillis());

    // stop server if SIGINT received
    server.setStopAtShutdown(true);
  }

  protected void initSpringContext() {
    contextHandler.setInitParameter("contextConfigLocation", getSpringContextLocations());
    initContextFilters(contextHandler);

    // add spring context load listener
    contextHandler.addEventListener(new ContextLoaderListener());

    initServlets(contextHandler);
  }
}
