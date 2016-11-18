package com.truward.brikar.common.executor;

import com.truward.brikar.common.log.LogUtil;
import org.slf4j.MDC;

/**
 * Standard values for thread parameters binder.
 *
 * @author Alexander Shabanov
 */
public enum StandardThreadParametersBinder implements ThreadParametersBinder {

  REQUEST_ID {
    @Override
    public Object getLocalObject() {
      return MDC.get(LogUtil.REQUEST_ID);
    }

    @Override
    public void setLocalObject(Object tag) {
      if (tag == null) {
        return;
      }

      MDC.put(LogUtil.REQUEST_ID, tag.toString());
    }

    @Override
    public void unsetLocalObject(Object tag) {
      if (tag == null) {
        return;
      }

      MDC.remove(LogUtil.REQUEST_ID);
    }
  }
}
