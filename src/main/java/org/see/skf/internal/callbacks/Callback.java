package org.see.skf.internal.callbacks;

import java.util.concurrent.FutureTask;

interface Callback<T> {
    void complete(T outcome);
    FutureTask<T> getTask();
}
