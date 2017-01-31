package com.truward.brikar.rpc.support;

import com.truward.brikar.rpc.RpcMethod;
import org.springframework.http.converter.HttpMessageConverter;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Exposes service proxy in a form of RPC.
 *
 * @author Alexander Shabanov
 */
public class ServiceInterfaceServletRpcBinding extends AbstractServletRpcBinding {
  private final Map<String, RpcMethod> methodMap;

  public ServiceInterfaceServletRpcBinding(List<HttpMessageConverter<?>> messageConverters,
                                           Class<?> serviceInterface,
                                           Object serviceProxy) {
    super(messageConverters);
    Objects.requireNonNull(serviceProxy, "serviceProxy");
    Objects.requireNonNull(serviceInterface, "serviceInterface");

    if (!serviceInterface.isInstance(serviceProxy)) {
      throw new IllegalArgumentException("serviceProxy is not an instance of " + serviceInterface);
    }

    this.methodMap = getCheckedMethodMap(serviceInterface, serviceProxy);
    setServiceName(serviceInterface.getSimpleName());
  }

  public ServiceInterfaceServletRpcBinding(List<HttpMessageConverter<?>> messageConverters,
                                           Object serviceProxy) {
    this(messageConverters, getInferredInteraceFromServiceProxy(serviceProxy), serviceProxy);
  }

  //
  // Private
  //

  private static Class<?> getInferredInteraceFromServiceProxy(Object serviceProxy) {
    Objects.requireNonNull(serviceProxy, "serviceProxy");
    final Class[] interfaces = serviceProxy.getClass().getInterfaces();
    if (interfaces.length != 1) {
      throw new IllegalArgumentException("Can't infer single implementing interface for given proxy of type=" +
          serviceProxy.getClass());
    }
    return interfaces[0];
  }

  private Map<String, RpcMethod> getCheckedMethodMap(Class<?> serviceInterface, Object serviceProxy) {
    final Method[] methods = serviceInterface.getMethods();

    final Map<String, RpcMethod> methodMap = new HashMap<>(methods.length * 2);
    for (final Method method : methods) {
      if (method.getDeclaringClass().equals(Object.class)) {
        continue; // exclude object methods
      }

      if (method.getParameterTypes().length != 1) {
        throw new IllegalStateException("Only input one parameter supported, offending method=" + method);
      }

      final RpcMethod oldMethod = methodMap.put(method.getName(), new ReflectionRpcMethod(method, serviceProxy));
      if (oldMethod != null) {
        log.warn("Overloaded methods are not supported in serviceInterface={}, method1={}, method2={}",
            serviceInterface, oldMethod, method);
      }
    }

    return methodMap;
  }

  @Override
  public Collection<RpcMethod> getExposedMethods() {
    return this.methodMap.values();
  }

  @ParametersAreNonnullByDefault
  private static final class ReflectionRpcMethod implements RpcMethod {

    private final Method method;
    private final Object serviceProxy;
    private final Class<?> inputType;

    public ReflectionRpcMethod(Method method, Object serviceProxy) {
      this.method = method;
      this.serviceProxy = serviceProxy;

      if (method.getParameterTypes().length != 1) {
        throw new IllegalStateException("Only input one parameter supported, offending method=" + method);
      }
      this.inputType = method.getParameterTypes()[0];
    }

    @Override
    public String getName() {
      return method.getName();
    }

    @Nullable
    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public Class<?> getInputType() {
      return inputType;
    }

    @Override
    public Class<?> getOutputType() {
      return method.getReturnType();
    }

    @Nullable
    @Override
    public Object getDefaultInputInstance() {
      return null;
    }

    @Override
    public Object call(Object input) throws Exception {
      try {
        return method.invoke(serviceProxy, input);
      } catch (IllegalAccessException e) {
        throw new IllegalStateException("Error while invoking RPC method", e);
      } catch (InvocationTargetException e) {
        if (e.getCause() instanceof Exception) {
          throw (Exception) e.getCause();
        }
        throw new IllegalStateException("Error while invoking RPC method", e);
      }
    }
  }
}
