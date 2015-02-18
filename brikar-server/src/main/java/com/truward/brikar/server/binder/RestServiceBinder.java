package com.truward.brikar.server.binder;

import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestOperations;

import java.lang.reflect.Method;

/**
 * @author Alexander Shabanov
 */
public class RestServiceBinder {

  private void createMethodHandler(Method method) {

    final RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
    if (requestMapping == null) {
      throw new BeanInitializationException("RequestMapping annotation is missing");
    }

    //requestMapping.method() <-- only one supported (or zero - in this case GET implied)
    //requestMapping.value() <-- only one or no mappings expected
    final RequestMethod[] methods = requestMapping.method();

    if (requestMapping.consumes().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.consumes is not supported");
    }

    if (requestMapping.headers().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.headers is not supported");
    }

    if (requestMapping.produces().length > 0) {
      throw new UnsupportedOperationException("RequestMapping.consumes is not supported");
    }
  }

  private void makeRequest() {
    RestOperations restOperations;
  }

}
