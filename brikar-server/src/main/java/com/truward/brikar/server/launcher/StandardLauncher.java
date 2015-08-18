package com.truward.brikar.server.launcher;

import com.truward.brikar.server.auth.SimpleAuthenticatorUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.context.StandardWebApplicationContextInitializer;
import com.truward.brikar.server.tracking.RequestIdAwareFilter;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

/**
 * Standard web application launcher.
 *
 * @author Alexander Shabanov
 */
public class StandardLauncher implements AutoCloseable {

  /**
   * A name of a property that should hold a port number value, default value is defined in {@link #DEFAULT_PORT}.
   */
  public static final String CONFIG_KEY_PORT = "brikar.settings.port";

  /**
   * Default value, which will be used if property {@link #CONFIG_KEY_PORT} does not exist.
   */
  public static final int DEFAULT_PORT = 8080;

  /**
   * A name of a property that should hold a numerical value that represents a time in milliseconds that server
   * should wait to handle incoming connections before shutting down.
   */
  public static final String CONFIG_KEY_SHUTDOWN_DELAY = "brikar.settings.gracefulShutdownMillis";

  /**
   * Default value, which should be used if property {@link #CONFIG_KEY_SHUTDOWN_DELAY} does not exist.
   */
  public static final int DEFAULT_SHUTDOWN_DELAY = 5000;

  /**
   * An optional system property that should contain a path to the comma separated property files that should override
   * default properties.
   * <p>
   * Sample values: <code>classpath:/prod.properties</code>,
   * <code>file:/opt/prod1.properties,file:/opt/prod2.properties</code>
   * </p>
   */
  public static final String SYS_PROP_SETTINGS_OVERRIDE = "brikar.settings.path";

  /**
   * Name of the file that should contain default property files.
   */
  public static final String DEFAULT_PROPERTIES_FILE_NAME = "core.properties";

  @Nonnull
  public static List<String> getConfigurationPaths(@Nonnull String defaultDirPrefix) {
    final List<String> paths = new ArrayList<>();

    paths.add(defaultDirPrefix + DEFAULT_PROPERTIES_FILE_NAME);

    final String overridePaths = System.getProperty(SYS_PROP_SETTINGS_OVERRIDE);
    if (overridePaths != null) {
      paths.addAll(Arrays.asList(overridePaths.split(",")));
    }

    return paths;
  }

  @Nonnull
  public static PropertySource<?> createPropertySource(@Nonnull List<String> configurationPaths) {
    final DefaultResourceLoader resourceLoader = new DefaultResourceLoader();

    final Properties properties = new Properties();
    try {
      for (final String configurationPath : configurationPaths) {
        final Resource resource = resourceLoader.getResource(configurationPath);
        if (!resource.exists()) {
          throw new IllegalStateException("Override properties file does not exist at " + configurationPath);
        }

        PropertiesLoaderUtils.fillProperties(properties, resource);
      }

      // initialize property source based on these properties
      return new PropertiesPropertySource("profile", properties);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  //
  // State
  //

  private final PropertyResolver propertyResolver;
  private final PropertySource<?> propertySource;
  private String propertySourceKey;
  private ServletContextHandler contextHandler;
  private String defaultDirPrefix;
  private boolean simpleSecurityEnabled;
  private boolean requestIdOperationsEnabled;
  private String authPropertiesPrefix = "auth";

  //
  // Public Methods
  //

  public StandardLauncher(@Nonnull PropertySource<?> propertySource, @Nonnull String defaultDirPrefix) {
    this.propertySource = propertySource;

    final MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addFirst(propertySource);
    this.propertyResolver = new PropertySourcesPropertyResolver(mutablePropertySources);

    // Loggers need to be configured as soon as possible, otherwise jetty will use its own default logger
    configureLoggers();

    setRequestIdOperationsEnabled(true);
    setDefaultDirPrefix(defaultDirPrefix);
  }

  public StandardLauncher(@Nonnull String defaultDirPrefix) {
    this(createPropertySource(getConfigurationPaths(defaultDirPrefix)), defaultDirPrefix);
  }

  public StandardLauncher() {
    this("classpath:/");
  }

  @Override
  public void close() {
    StandardWebApplicationContextInitializer.removePropertySourceByKey(propertySourceKey);
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
  public StandardLauncher setAuthPropertiesPrefix(@Nonnull String authPropertiesPrefix) {
    this.authPropertiesPrefix = authPropertiesPrefix;
    return this;
  }

  @Nonnull
  public PropertyResolver getPropertyResolver() {
    return propertyResolver;
  }

  public final void start() throws Exception {
    final int port = propertyResolver.getProperty(CONFIG_KEY_PORT, Integer.class, DEFAULT_PORT);
    getLogger().info("About to start server. Use port={}", port);

    final Server server = new Server(port);
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

  @Nonnull
  public StandardLauncher setDefaultDirPrefix(@Nonnull String defaultDirPrefix) {
    Assert.notNull(defaultDirPrefix, "defaultDirPrefix can't be null");
    this.defaultDirPrefix = defaultDirPrefix;
    return this;
  }


  //
  // Protected
  //

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

  /**
   * This method initializes servlet filters.
   * <p>
   * Default implementation adds RequestId filter by default.
   * </p>
   * <p>
   * Overrides of this method can also do things like enforcing UTF-8 encoding.
   * Here is an example of how it can be done:
   * <code>
   *   final FilterHolder encFilterHolder = contextHandler.addFilter(CharacterEncodingFilter.class, "/*", EnumSet.allOf(DispatcherType.class));
   *   encFilterHolder.setInitParameter("encoding", "UTF-8");
   *   encFilterHolder.setInitParameter("forceEncoding", "true"); // <-- this line instructs filter to add encoding
   * </code>
   * However this is usually not something
   * </p>
   *
   * @param contextHandler Servlet context handler
   */
  protected void initContextFilters(@Nonnull ServletContextHandler contextHandler) {
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
    final List<SimpleServiceUser> users = SimpleAuthenticatorUtil.loadUsers(propertySource, authPropertiesPrefix);
    if (!users.isEmpty()) {
      return users;
    }

    // fallback: there is no settings, use default username and generate password on the fly
    // normally this is not something service owner should rely on
    final Random random = new SecureRandom();
    final String username = "serviceuser";
    final String password = Long.toHexString(random.nextLong());
    getLogger().warn("Simple security: no predefined configuration; using username={} and password={}",
        username, password);

    return Collections.singletonList(new SimpleServiceUser(username, password));
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
    final int shutdownDelay = propertyResolver
        .getProperty(CONFIG_KEY_SHUTDOWN_DELAY, Integer.class, DEFAULT_SHUTDOWN_DELAY);
    getLogger().info("Using shutdownDelay={}", shutdownDelay);

    server.setGracefulShutdown(shutdownDelay);

    // stop server if SIGINT received
    server.setStopAtShutdown(true);
  }

  protected void initSpringContext() {
    contextHandler.setInitParameter("contextConfigLocation", getSpringContextLocations());

    propertySourceKey = StandardWebApplicationContextInitializer
        .registerPropertySource(new StandardWebApplicationContextInitializer.ServletInitParameterSetter() {
          @Override
          public void setInitParameter(@Nonnull String key, @Nonnull String value) {
            contextHandler.setInitParameter(key, value);
          }
        }, propertySource);

    contextHandler.setInitParameter("contextInitializerClasses",
        StandardWebApplicationContextInitializer.class.getName());

    initContextFilters(contextHandler);

    // add spring context load listener
    contextHandler.addEventListener(new ContextLoaderListener());

    initServlets(contextHandler);
  }
}
