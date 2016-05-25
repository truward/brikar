package com.truward.brikar.rpc;

import javax.annotation.Nonnull;
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

  @Nonnull
  String getServiceName();

  void process(@Nonnull HttpServletRequest request,
               @Nonnull HttpServletResponse response) throws ServletException, IOException;

  @Nonnull
  List<String> getExposedMethodNames();
}
