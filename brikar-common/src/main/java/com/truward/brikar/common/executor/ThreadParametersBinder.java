package com.truward.brikar.common.executor;

/**
 * An abstraction over the entity which is capable to extract and propagate thread local variables from one thread
 * to the other.
 *
 * @author Alexander Shabanov
 */
public interface ThreadParametersBinder {

  Object getLocalObject();

  void setLocalObject(Object tag);

  void unsetLocalObject(Object tag);
}
