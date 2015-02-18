package com.truward.brikar.common.executor;

/**
 * An abstraction over the entity which is capable to extract and propagate thread local variables from one thread
 * to the other.
 *
 * @author Alexander Shabanov
 */
public interface ThreadParametersBinder {

  Object getLocalObjects();

  void setLocalObjects(Object tag);

  void unsetLocalObjects(Object tag);
}
