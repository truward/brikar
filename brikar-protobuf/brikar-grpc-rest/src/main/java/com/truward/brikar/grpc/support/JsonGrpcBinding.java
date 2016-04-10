package com.truward.brikar.grpc.support;

import com.truward.brikar.grpc.ServletRpcBinding;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Alexander Shabanov
 */
public class JsonGrpcBinding implements ServletRpcBinding {
  public static final String METHOD_PARAM_NAME = "m";



  @Override
  public void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    if (!"POST".equals(request.getMethod())) {
      throw new ServletException("Only POST methods supported");
    }

    final String methodName = request.getParameter(METHOD_PARAM_NAME);
  }
}
