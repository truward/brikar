package com.truward.brikar.rpc;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * An abstraction that represents RPC-over-HTTP service.
 * 
 * @author Alexander Shabanov
 */
public interface ServletRpcBinding {

  String getServiceName();

  void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

  List<String> getExposedMethodNames();
}
