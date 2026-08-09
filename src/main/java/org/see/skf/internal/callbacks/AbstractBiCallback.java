package org.see.skf.internal.callbacks;

import java.util.concurrent.FutureTask;

abstract class AbstractBiCallback<T,U> {
    private final T target;
    private U outcome;
    private final FutureTask<U> task;

    protected AbstractBiCallback(T target, U outcome) {
        this.target = target;
        this.outcome = outcome;
        this.task = createTask();
    }

    protected abstract FutureTask<U> createTask();

    public T getTarget() {
        return this.target;
    }

    public synchronized U getOutcome() {
        return this.outcome;
    }

    public synchronized void complete(U outcome) {
        this.outcome = outcome;
    }

    public FutureTask<U> getTask() {
        return this.task;
    }
}
