package org.see.skf.internal.callbacks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

final class CallbackImpl<T> implements Callback<T> {

    private T outcome;
    private final FutureTask<T> task;

    private final CountDownLatch latch;

    CallbackImpl(int latchCount) {
        this.task = createTask();
        this.latch = new CountDownLatch(latchCount);
    }

    private FutureTask<T> createTask() {
        return new FutureTask<>(() -> {
            this.latch.await();
            return this.outcome;
        });
    }

    @Override
    public synchronized void complete(T outcome) {
        this.outcome = outcome;
        this.latch.countDown();
    }

    @Override
    public FutureTask<T> getTask() {
        return this.task;
    }
}
