package com.truward.brikar.rpc.servlet;

import com.truward.brikar.rpc.explorer.RpcServiceExplorerController;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Experimental servlet, that exposes RPC API + explorer.
 */
public class RpcServiceServlet extends HttpServlet {
  private static final String API_PREFIX = "/rpc/api";
  private static final String EXPLORER_PREFIX = "/rpc/explorer";

  private RpcServiceExplorerController rpcController;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    final ApplicationContext applicationContext = WebApplicationContextUtils
        .getWebApplicationContext(getServletContext());

    this.rpcController = applicationContext.getBean(RpcServiceExplorerController.class);
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    if (tryServeRpc(req, resp)) {
      return;
    }

    if (tryServeExplorer(req, resp)) {
      return;
    }

    super.service(req, resp);
  }

  //
  // Private
  //

  private boolean tryServeRpc(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String requestUri = req.getRequestURI();
    if (!requestUri.startsWith(API_PREFIX)) {
      return false;
    }

    final int serviceNameStart = requestUri.indexOf('/', API_PREFIX.length());
    if (serviceNameStart < 0) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    final int methodNameStart = requestUri.indexOf('/', serviceNameStart + 1);
    if (methodNameStart < 0) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    final String serviceName = requestUri.substring(serviceNameStart + 1, methodNameStart);
    final String methodName = requestUri.substring(methodNameStart + 1);

    // now invoke the controller
    rpcController.invoke(serviceName, methodName, req, resp);
    return true;
  }

  private boolean tryServeExplorer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    final String requestUri = req.getRequestURI();
    if (!requestUri.startsWith(EXPLORER_PREFIX)) {
      return false;
    }

    final int serviceNameStart = requestUri.indexOf('/', API_PREFIX.length());
    if (serviceNameStart < 0) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND);
      return true;
    }

    HttpMethod method = HttpMethod.resolve(req.getMethod());
    if (method != HttpMethod.GET) {
      resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
      return true;
    }

    final String serviceName = requestUri.substring(serviceNameStart + 1);
    rpcController.explorer(serviceName, resp);
    return true;
  }
}
