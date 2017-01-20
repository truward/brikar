package com.truward.brikar.common.executor;

import com.truward.brikar.common.log.LogUtil;
import com.truward.brikar.common.log.metric.MetricsCollection;
import org.slf4j.MDC;

/**
 * Standard values for thread parameters binder.
 *
 * @author Alexander Shabanov
 */
public enum StandardThreadParametersBinder implements ThreadParametersBinder {

  REQUEST_VECTOR {
    @Override
    public Object getLocalObject() {
      return MDC.get(LogUtil.REQUEST_VECTOR);
    }

    @Override
    public void setLocalObject(Object tag) {
      if (tag == null) {
        return;
      }

      MDC.put(LogUtil.REQUEST_VECTOR, tag.toString());
    }

    @Override
    public void unsetLocalObject(Object tag) {
      if (tag == null) {
        return;
      }

      MDC.remove(LogUtil.REQUEST_VECTOR);
    }
  },

  METRICS_COLLECTION {
    @Override
    public Object getLocalObject() {
      return LogUtil.getLocalMetricsCollection();
    }

    @Override
    public void setLocalObject(Object tag) {
      if (tag == null) {
        return;
      }

      LogUtil.setLocalMetricsCollection((MetricsCollection) tag);
    }

    @Override
    public void unsetLocalObject(Object tag) {
      if (tag == null) {
        return;
      }

      LogUtil.setLocalMetricsCollection(null);
    }
  }
}
