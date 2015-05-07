package com.truward.brikar.server.launcher;

import com.truward.brikar.server.args.StandardArgParser;
import com.truward.brikar.server.args.StartArgs;
import com.truward.brikar.server.auth.SimpleAuthenticatorUtil;
import com.truward.brikar.server.auth.SimpleServiceUser;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.annotation.Nonnull;
import javax.servlet.DispatcherType;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

/**
 * @author Alexander Shabanov
 */
public class StandardLauncher {
  private ServletContextHandler contextHandler;
  private String defaultDirPrefix;
  private boolean simpleSecurityEnabled;
  private String authPropertiesPrefix = "auth";

  public StandardLauncher(@Nonnull String defaultDirPrefix) {
    // Loggers need to be configured as soon as possible, otherwise jetty will use its own default logger
    configureLoggers();

    setDefaultDirPrefix(defaultDirPrefix);
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
  public StandardLauncher setAuthPropertiesPrefix(@Nonnull String authPropertiesPrefix) {
    this.authPropertiesPrefix = authPropertiesPrefix;
    return this;
  }

  public final void start(@Nonnull String[] args) throws Exception {
    final StandardArgParser argParser = new StandardArgParser(args, defaultDirPrefix + "default.properties");
    final int result = argParser.parse();
    if (result != 0) {
      System.exit(result);
      return;
    }


    if (!argParser.isReadyToStart()) {
      return;
    }

    startServer(argParser.getStartArgs());
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
    System.setProperty("logback.configurationFile", "default-service-logback.xml");
  }

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

    // Spring security
    if (isSpringSecurityEnabled()) {
      final FilterHolder delegatingFilterHolder = new FilterHolder(DelegatingFilterProxy.class);
      delegatingFilterHolder.setName("springSecurityFilterChain");
      contextHandler.addFilter(delegatingFilterHolder, "/*", EnumSet.allOf(DispatcherType.class));
    }
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
    // [1] Try to use system variable
    String path = System.getProperty("brikar.settings.simpleSecuritySettingsFile");
    if (path != null) {
      return SimpleAuthenticatorUtil.loadUsers(new File(path), authPropertiesPrefix);
    }

    // [2] Try to load from the default location
    final String authResourceLocation = defaultDirPrefix + "auth.properties";
    getLogger().info("Simple security: trying to load users from {}", authResourceLocation);
    final ResourceLoader loader = new DefaultResourceLoader();
    final org.springframework.core.io.Resource resource = loader.getResource(authResourceLocation);
    if (resource.exists()) {
      return SimpleAuthenticatorUtil.loadUsers(resource.getInputStream(),
          SimpleAuthenticatorUtil.DEFAULT_CHARSET, authPropertiesPrefix);
    } else {
      getLogger().warn("Simple security: missing configuration at {}", authResourceLocation);
    }

    // [3] Fallback: there is no settings, use default username and generate password on the fly
    final SecureRandom random = new SecureRandom();
    final String username = "serviceuser";
    final String password = Long.toHexString(random.nextLong());
    getLogger().warn("Simple security: using username={} and password={}", username, password);
    return Collections.singletonList(new SimpleServiceUser(username, password));
  }

  //
  // Private
  //

  private void startServer(@Nonnull StartArgs startArgs) throws Exception {
    System.setProperty("brikar.settings.path", startArgs.getConfigPath());

    final Server server = new Server(startArgs.getPort());
    server.setSendServerVersion(false);

    //noinspection PointlessBitwiseExpression
    contextHandler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS | ServletContextHandler.NO_SECURITY);
    contextHandler.setContextPath("/");
    initSpringContext();

    final HandlerCollection handlerList = new HandlerCollection();
    final List<Handler> handlers = getHandlers();
    handlerList.setHandlers(handlers.toArray(new Handler[handlers.size()]));
    server.setHandler(handlerList);

    server.start();
    server.join();
  }



  private void initSpringContext() {
    contextHandler.setInitParameter("contextConfigLocation", getSpringContextLocations());
    initContextFilters(contextHandler);

    // add spring context load listener
    contextHandler.addEventListener(new ContextLoaderListener());

    initServlets(contextHandler);
  }
}
