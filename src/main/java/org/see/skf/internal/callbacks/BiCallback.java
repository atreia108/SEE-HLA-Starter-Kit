package org.see.skf.internal.callbacks;

import java.util.concurrent.FutureTask;

interface BiCallback<T,U> {
    void complete(U outcome);
    T getTarget();
    FutureTask<U> getTask();
}
