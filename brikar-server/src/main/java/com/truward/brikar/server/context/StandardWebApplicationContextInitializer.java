package com.truward.brikar.server.context;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Application context initializer that contains standard logic for initializing application properties.
 *
 * <p>
 * The reason why this class has {@link #PROPERTIES} and complicated code around it including static helper
 * methods is because only strings can be passed on the servlet initialization stage and what we want is to
 * associate {@link PropertySource} with {@link ConfigurableApplicationContext} created on servlet initialization
 * stage.
 * </p>
 * <p>
 * Since these properties should be created *before* initializing servlet context (they contain things like port
 * number, server shutdown timeout and authorization settings) we can't actually initialize them along with the
 * context, hence the complexity.
 * </p>
 *
 * @author Alexander Shabanov
 */
public final class StandardWebApplicationContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  private static final ConcurrentHashMap<String, PropertySource<?>> PROPERTIES = new ConcurrentHashMap<>();

  /**
   * Property which will be known to
   */
  private static final String PROPERTY_SOURCE_SERVLET_CONFIG_KEY = "brikarPropertySource";

  /**
   * Callback interface that caller supposed to support. Implementation is as simple as just calling
   * ServletContext.setInitParameter. The reason why complex callback schema is used here is because standard
   * {@link javax.servlet.ServletContext} is not available at the moment of jetty context initialization however
   * jetty-specific initialization works.
   * <p>
   * So, in order to make this implementation to be container-agnostic (Tomcat, Jetty, Resin) we don't have
   * anything specific to jetty here.
   * </p>
   */
  public interface ServletInitializer {
    void setInitParameter(@Nonnull String key, @Nonnull String value);
  }

  /**
   * Encapsulates the logic for associating property source with a given servlet.
   *
   * @param servletInitializer A callback that encapsulates the call to the container specific way to set init parameter
   * @param source Property source, that needs to be registered and later available to the spring context
   * @return A closeable instance that should be closed to perform cleanup once server is stopped
   */
  @Nonnull
  public static AutoCloseable register(@Nonnull ServletInitializer servletInitializer,
                                       @Nonnull PropertySource<?> source) {
    final BigInteger bigInteger = new BigInteger(64, ThreadLocalRandom.current());
    final String key = bigInteger.toString(16);

    if (PROPERTIES.containsKey(key)) {
      return register(servletInitializer, source); // shouldn't happen
    }

    // associate this key with the context parameter
    servletInitializer.setInitParameter(PROPERTY_SOURCE_SERVLET_CONFIG_KEY, key);
    // ... and put it to the globally visible properties map
    PROPERTIES.put(key, source);

    return new AutoCloseable() {
      @Override
      public void close() {
        PROPERTIES.remove(key);
      }
    };
  }

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    // get property from the app environment - it should automatically result in ServletContext scanning
    final String key = applicationContext.getEnvironment()
        .getProperty(StandardWebApplicationContextInitializer.PROPERTY_SOURCE_SERVLET_CONFIG_KEY);
    if (key == null) {
      throw new IllegalStateException("There is no property source key associated with " +
          PROPERTY_SOURCE_SERVLET_CONFIG_KEY + " in current ServletConfig");
    }

    final PropertySource<?> propertySource = PROPERTIES.get(key);
    if (propertySource == null) {
      throw new IllegalStateException("There is no resource associated with a key=" + key);
    }

    applicationContext.getEnvironment().getPropertySources().addFirst(propertySource);
  }
}
