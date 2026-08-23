package org.see.skf.internal.callbacks;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

abstract class AbstractCallback<T> {

    private T outcome;
    private final FutureTask<T> task;

    private final CountDownLatch latch;

    protected AbstractCallback(T outcome, int latchCount) {
        this.outcome = outcome;
        this.task = createTask();
        this.latch = new CountDownLatch(latchCount);
    }

    private FutureTask<T> createTask() {
        return new FutureTask<>(() -> {
            this.latch.await();
            return this.outcome;
        });
    }

    public final synchronized void complete(T outcome) {
        this.outcome = outcome;
        this.latch.countDown();
    }

    public final FutureTask<T> getTask() {
        return this.task;
    }
}

