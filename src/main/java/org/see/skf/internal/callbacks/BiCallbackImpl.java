package org.see.skf.internal.callbacks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

final class BiCallbackImpl<T,U> implements BiCallback<T,U> {
    private final T target;
    private U outcome;
    private final FutureTask<U> task;

    private final CountDownLatch latch;

    BiCallbackImpl(T target, int latchCount) {
        this.target = target;
        this.task = createTask();

        this.latch = new CountDownLatch(latchCount);
    }

    private FutureTask<U> createTask() {
        return new FutureTask<>(() -> {
            this.latch.await();
            return this.outcome;
        });
    }

    @Override
    public T getTarget() {
        return this.target;
    }

    @Override
    public synchronized void complete(U outcome) {
        this.outcome = outcome;
        this.latch.countDown();
    }

    @Override
    public FutureTask<U> getTask() {
        return this.task;
    }
}
