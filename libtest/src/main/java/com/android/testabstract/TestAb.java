package com.android.testabstract;

public abstract class TestAb<T> {
    public final void onExecuted(T paramT, Exception paramException) {
        onSuccess(paramT);
    }

    protected abstract void onSuccess(T paramT);
}
