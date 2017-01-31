package com.truward.brikar.rpc;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Represents a generic interface to the RPC method that can be called.
 */
@ParametersAreNonnullByDefault
public interface RpcMethod {

  String getName();

  @Nullable
  String getDescription();

  Class<?> getInputType();

  Class<?> getOutputType();

  @Nullable
  Object getDefaultInputInstance();

  Object call(Object input) throws Exception;
}
