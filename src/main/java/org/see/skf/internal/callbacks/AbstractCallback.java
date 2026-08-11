package org.see.skf.internal.callbacks;

import java.util.concurrent.FutureTask;

abstract class AbstractCallback<T> {

    private T outcome;
    private final FutureTask<T> task;

    protected AbstractCallback(T outcome) {
        this.outcome = outcome;
        this.task = createTask();
    }

    protected abstract FutureTask<T> createTask();

    public synchronized T getOutcome() {
        return this.outcome;
    }

    public synchronized void complete(T outcome) {
        this.outcome = outcome;
    }

    public FutureTask<T> getTask() {
        return this.task;
    }
}
