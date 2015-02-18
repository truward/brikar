package com.truward.brikar.common.executor;


import com.truward.brikar.common.log.LogUtil;
import org.slf4j.MDC;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
  * @author Alexander Shabanov
 */
public enum StandardThreadParametersBinder implements ThreadParametersBinder {
  MDC_OPERATION_ID {
    @Override
    public Object getLocalObjects() {
      return MDC.get(LogUtil.OPERATION_ID);
    }

    @Override
    public void setLocalObjects(Object tag) {
      if (tag == null) {
        return;
      }

      LogUtil.putOperationIdToMdc(tag.toString());
    }

    @Override
    public void unsetLocalObjects(Object tag) {
      if (tag == null) {
        return;
      }

      LogUtil.removeOperationIdFromMdc();
    }
  },

  REQUEST_SCOPE {
    @Override
    public Object getLocalObjects() {
      return RequestContextHolder.getRequestAttributes();
    }

    @Override
    public void setLocalObjects(Object tag) {
      final RequestAttributes requestAttributes = (RequestAttributes) tag;
      RequestContextHolder.setRequestAttributes(requestAttributes);
    }

    @Override
    public void unsetLocalObjects(Object tag) {
      RequestContextHolder.resetRequestAttributes();
    }
  }
}
