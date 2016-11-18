package com.truward.brikar.common.executor;

import org.springframework.core.task.AsyncTaskExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Task execution wrapper, which is capable of propagating certain objects from current thread to the child threads.
 * The primary purpose of this task executor wrapper is to propagate certain values to the new thread, such as
 * originating request ID and source request ID.
 *
 * @author Alexander Shabanov
 */
public final class ThreadLocalPropagatingTaskExecutor implements AsyncTaskExecutor {
  private final AsyncTaskExecutor delegate;
  private final List<ThreadParametersBinder> threadParametersBinders;

  public ThreadLocalPropagatingTaskExecutor(AsyncTaskExecutor delegate,
                                            List<? extends ThreadParametersBinder> threadParametersBinders) {
    this.delegate = Objects.requireNonNull(delegate, "delegate") ;
    this.threadParametersBinders = Collections.unmodifiableList(new ArrayList<>(Objects
        .requireNonNull(threadParametersBinders, "threadParameterBinders")));
  }

  @Override
  public void execute(Runnable task, long startTimeout) {
    delegate.execute(new DelegatedRunnable(task, threadParametersBinders), startTimeout);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return delegate.submit(new DelegatedRunnable(task, threadParametersBinders));
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return delegate.submit(new DelegatedCallable<>(task, threadParametersBinders));
  }

  @Override
  public void execute(Runnable task) {
    delegate.submit(new DelegatedRunnable(task, threadParametersBinders));
  }

  //
  // Private
  //

  private static abstract class DelegatedInvokerHelper {
    private final List<ThreadParametersBinder> binders;
    private final Object[] binderParameters;

    DelegatedInvokerHelper(List<ThreadParametersBinder> binders) {
      this.binders = binders;
      this.binderParameters = new Object[binders.size()];
      for (int i = 0; i < binders.size(); ++i) {
        this.binderParameters[i] = binders.get(i).getLocalObject();
      }
    }

    void bindAttributes() {
      for (int i = 0; i < binders.size(); ++i) {
        this.binders.get(i).setLocalObject(this.binderParameters[i]);
      }
    }

    void unbindAttributes() {
      // TODO: check for current thread?
      for (int i = 0; i < binders.size(); ++i) {
        this.binders.get(i).unsetLocalObject(this.binderParameters[i]);
      }
    }
  }

  private static final class DelegatedRunnable extends DelegatedInvokerHelper implements Runnable {
    private final Runnable delegate;

    DelegatedRunnable(Runnable delegate, List<ThreadParametersBinder> binders) {
      super(binders);
      this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public void run() {
      try {
        bindAttributes();
        delegate.run();
      } finally {
        unbindAttributes();
      }
    }
  }

  private static final class DelegatedCallable<T> extends DelegatedInvokerHelper implements Callable<T> {
    private final Callable<T> delegate;

    DelegatedCallable(Callable<T> delegate, List<ThreadParametersBinder> binders) {
      super(binders);
      this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    @Override
    public T call() throws Exception {
      try {
        bindAttributes();
        return delegate.call();
      } finally {
        unbindAttributes();
      }
    }
  }
}
