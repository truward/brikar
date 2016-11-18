package com.truward.brikar.server.launcher;

import com.truward.brikar.server.auth.SimpleAuthenticatorUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import com.truward.brikar.server.context.StandardWebApplicationContextInitializer;
import com.truward.brikar.server.tracking.RequestIdAwareFilter;
import com.truward.brikar.server.util.JettyResourceUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.*;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Callable;

/**
 * Standard web application launcher.
 *
 * <p>Uses jetty as a servlet container and makes a lot of assumptions about
 * an application which should be started, namely:</p>
 * <ul>
 *   <li><strong>Logger</strong> - by default logback is used and its default configuration is defined
 *   in <tt>default-service-logback.xml</tt></li>
 *   <li><strong>Dependency injection</strong> - spring and spring MVC are supposed to be used</li>
 *   <li><strong>Standard HTTP filters</strong> - for logging request/response-specific data</li>
 *   <li><strong>Configuration layout</strong> - standard configuration implies <tt>service.xml</tt>,
 *   <tt>webmvc.xml</tt> spring configs and <tt>core.properties</tt> file that contains default service
 *   configuration properties</li>
 * </ul>
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
   * A path to static resources for resources, served by static servlet.
   */
  public static final String CONFIG_KEY_STATIC_PATH = "brikar.settings.staticPath";

  /**
   * A path to static resources that will be used to override default locations for resources, served by static
   * servlet.
   */
  public static final String CONFIG_KEY_OVERRIDE_STATIC_PATH = "brikar.dev.overrideStaticPath";

  /**
   * Name of the file that should contain default property files.
   */
  public static final String DEFAULT_PROPERTIES_FILE_NAME = "core.properties";

  /**
   * A relative path, where launcher expect to find static resources, served by {@link ResourceHandler} if
   * {@link #staticHandlerEnabled} property has been set to true.
   * <p>
   * This value can be overridden by properties {@link #CONFIG_KEY_STATIC_PATH} and
   * {@link #CONFIG_KEY_OVERRIDE_STATIC_PATH}.
   * </p>
   */
  public static final String DEFAULT_STATIC_WEB_FOLDER = "web/static";

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
      throw new IllegalStateException("Error while creating property source", e);
    }
  }

  //
  // State
  //

  private final PropertyResolver propertyResolver;
  private final PropertySource<?> propertySource;
  private final String defaultDirPrefix;
  private AutoCloseable propertySourceCloseableRegistration;
  private ServletContextHandler contextHandler;
  private boolean simpleSecurityEnabled;
  private boolean requestIdOperationsEnabled;
  private String authPropertiesPrefix = "auth";
  private boolean springSecurityEnabled;
  private boolean staticHandlerEnabled;
  private int servletContextOptions;

  //
  // Public Methods
  //

  /**
   * Constructor, that initializes both property source and default configuration directory prefix.
   *
   * <p><strong>IMPORTANT:</strong>This constructor initializes loggers, it is very important to not to do
   * anything which directly or indirectly results in calls to the slf4j logger prior to calling this constructor</p>
   * <p>This is the reason why this method takes Callable instance and not just the PropertySource as
   * standard spring's implementation of PropertySource uses loggers even in class constructors.</p>
   *
   * <p>If it is very inconvenient or impossible, then explicit call to {@link #ensureLoggersConfigured()}
   * is advised as first operation after starting an application. This will perform default loggers configuration if
   * external properties do not override it.</p>
   *
   * @param propertySourceCallable A method, that should create and return property source instance,
   *                               which will be used while initializing server and propagated to the spring
   *                               context environment and used as property placeholder provider
   * @param defaultDirPrefix Path where project configuration is located,
   *                         for example <code>"classpath:/myService/"</code>
   * @throws Exception if unable to perform initialization
   */
  public StandardLauncher(@Nonnull Callable<PropertySource<?>> propertySourceCallable,
                          @Nonnull String defaultDirPrefix) throws Exception {
    // Loggers need to be configured as soon as possible, otherwise jetty will use its own default logger
    configureLoggers();

    if (defaultDirPrefix.endsWith("/")) {
      this.defaultDirPrefix = defaultDirPrefix;
    } else {
      this.defaultDirPrefix = defaultDirPrefix + '/';
    }

    this.propertySource = propertySourceCallable.call();

    final MutablePropertySources mutablePropertySources = new MutablePropertySources();
    mutablePropertySources.addFirst(propertySource);
    this.propertyResolver = new PropertySourcesPropertyResolver(mutablePropertySources);

    setRequestIdOperationsEnabled(true);
    setSessionsEnabled(false);
    setContextSecurityEnabled(false);
  }

  /**
   * Constructor that uses default way of initializing property source.
   * For more details see
   *
   * @param defaultDirPrefix Path where project configuration is located,
   *                         for example <code>"classpath:/myService/"</code>
   * @throws Exception if unable to perform initialization
   */
  public StandardLauncher(@Nonnull final String defaultDirPrefix) throws Exception {
    this(new Callable<PropertySource<?>>() {
      @Override
      public PropertySource<?> call() throws Exception {
        return createPropertySource(getConfigurationPaths(defaultDirPrefix));
      }
    }, defaultDirPrefix);
  }

  @Override
  public void close() throws Exception {
    if (propertySourceCloseableRegistration != null) {
      propertySourceCloseableRegistration.close();
      propertySourceCloseableRegistration = null;
    }
  }

  @Nonnull
  public StandardLauncher setSimpleSecurityEnabled(boolean enabled) {
    this.simpleSecurityEnabled = enabled;
    return this;
  }

  @Nonnull
  public StandardLauncher setSpringSecurityEnabled(boolean enabled) {
    this.springSecurityEnabled = enabled;
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

  /**
   * Enables session support.
   * See also {@link ServletContextHandler#SESSIONS}.
   *
   * @param enabled Identifies whether sessions should be enabled
   * @return Launcher instance for chaining
   */
  @Nonnull
  public StandardLauncher setSessionsEnabled(boolean enabled) {
    toggleServletContextHandlerParameter(enabled, ServletContextHandler.SESSIONS);
    return this;
  }

  /**
   * Enables builtin Jetty security.
   * See also {@link ServletContextHandler#SECURITY}.
   *
   * @param enabled Identifies whether context security should be enabled
   * @return Launcher instance for chaining
   */
  @Nonnull
  public StandardLauncher setContextSecurityEnabled(boolean enabled) {
    toggleServletContextHandlerParameter(enabled, ServletContextHandler.SECURITY);
    return this;
  }

  @Nonnull
  public StandardLauncher setStaticHandlerEnabled(boolean enabled) {
    this.staticHandlerEnabled = enabled;
    return this;
  }

  @Nonnull
  public PropertyResolver getPropertyResolver() {
    return propertyResolver;
  }

  public final StandardLauncher start() throws Exception {
    final int port = propertyResolver.getProperty(CONFIG_KEY_PORT, Integer.class, DEFAULT_PORT);
    getLogger().info("About to start server. Use port={}", port);

    final Server server = new Server(port);
    setServerSettings(server);

    contextHandler = new ServletContextHandler(servletContextOptions);
    contextHandler.setContextPath("/");
    //contextHandler.setSessionHandler();
    initSpringContext();

    final HandlerCollection handlerList = new HandlerCollection();
    final List<Handler> handlers = getHandlers();
    handlerList.setHandlers(handlers.toArray(new Handler[handlers.size()]));
    server.setHandler(handlerList);

    setShutdownStrategy(server);

    server.start();
    server.join();
    return this;
  }

  /**
   * Default logback initialization. Should be called explicitly if there is a code that calls or creates loggers
   * directly or indirectly.
   */
  public static void ensureLoggersConfigured() {
    // initialize logback if logback.configurationFile property has not been set
    if (System.getProperty("logback.configurationFile") == null) {
      System.setProperty("logback.configurationFile", "default-service-logback.xml");
    }
  }

  //
  // Protected
  //

  @Nonnull
  protected Logger getLogger() {
    return LoggerFactory.getLogger(getClass());
  }

  protected void configureLoggers() {
    ensureLoggersConfigured();
  }

  @Nonnull
  protected List<Handler> getHandlers() throws Exception {
    final List<Handler> result = new ArrayList<>();
    result.add(contextHandler);
    if (staticHandlerEnabled) {
      result.add(createStaticHandler());
    }
    return result;
  }

  @Nonnull
  protected String getSpringContextLocations() {
    return defaultDirPrefix + "spring/service.xml";
  }

  @Nonnull
  protected String getDispatcherServletConfigLocations() {
    return defaultDirPrefix + "spring/webmvc.xml";
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
   *   encFilterHolder.setInitParameter("forceEncoding", "true"); // this line instructs filter to add encoding
   * </code>
   * However this is usually not something
   * </p>
   *
   * @param contextHandler Servlet context handler
   */
  protected void initContextFilters(@Nonnull ServletContextHandler contextHandler) {
    if (springSecurityEnabled) {
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
        "/g/*,/api/*,/j_spring_security_check");
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

  @Nonnull
  protected ResourceHandler createStaticHandler() throws IOException {
    final ResourceHandler resourceHandler = new ResourceHandler();

    String staticPath = getPropertyResolver().getProperty(CONFIG_KEY_OVERRIDE_STATIC_PATH);
    if (staticPath == null) {
      staticPath = getPropertyResolver().getProperty(CONFIG_KEY_STATIC_PATH);
    }

    if (staticPath != null) {
      getLogger().info("Using override path for static resources: {}", staticPath);
      resourceHandler.setBaseResource(JettyResourceUtil.createResource(staticPath));
    } else {
      resourceHandler.setBaseResource(getDefaultStaticResource());
    }

    return resourceHandler;
  }

  @Nonnull
  protected org.eclipse.jetty.util.resource.Resource getDefaultStaticResource() throws IOException {
    return JettyResourceUtil.createResource(defaultDirPrefix + DEFAULT_STATIC_WEB_FOLDER);
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

    propertySourceCloseableRegistration = StandardWebApplicationContextInitializer
        .register(new StandardWebApplicationContextInitializer.ServletInitializer() {
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

  //
  // Private
  //

  private void toggleServletContextHandlerParameter(boolean enabled, int param) {
    if (enabled) {
      this.servletContextOptions |= param;
    } else {
      this.servletContextOptions &= ~param;
    }
  }
}
