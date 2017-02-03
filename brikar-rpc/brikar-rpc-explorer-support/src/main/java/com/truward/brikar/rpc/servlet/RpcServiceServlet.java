package com.truward.brikar.rpc.servlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet, that exposes RPC support.
 */
public class RpcServiceServlet extends HttpServlet {
  private final Logger log = LoggerFactory.getLogger(getClass());
  private ApplicationContext applicationContext;

  public RpcServiceServlet() {
    log.info("Creating RPC service servlet");
  }

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    applicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.service(req, resp);
  }
}
