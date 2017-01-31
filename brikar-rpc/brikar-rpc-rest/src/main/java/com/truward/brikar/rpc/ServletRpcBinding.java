package com.truward.brikar.rpc;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * An abstraction that represents RPC-over-HTTP service.
 * 
 * @author Alexander Shabanov
 */
@ParametersAreNonnullByDefault
public interface ServletRpcBinding {

  String getServiceName();

  void process(@Nullable String urlMethodPath,
               HttpServletRequest request,
               HttpServletResponse response) throws IOException;

  @Deprecated
  default Collection<String> getExposedMethodNames() {
    return getExposedMethods().stream().map(RpcMethod::getName).collect(Collectors.toList());
  }

  Collection<RpcMethod> getExposedMethods();
}
