package org.see.skf.internal.callbacks;

import java.util.concurrent.FutureTask;

interface IAbstractCallback<T> {
    void complete(T outcome);
    FutureTask<T> getTask();
}
