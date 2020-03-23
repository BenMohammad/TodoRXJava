package com.benmohammad.todorxjava.util;


import androidx.test.espresso.*;

import java.util.concurrent.atomic.AtomicInteger;


public final class SimpleCountingIdlingResource implements IdlingResource {

    private final String mResourceName;

    private final AtomicInteger counter = new AtomicInteger(0);
    private volatile ResourceCallback resourceCallback;

    public SimpleCountingIdlingResource(String resourceName) {
        this.mResourceName = resourceName;
    }

    @Override
    public String getName() {
        return mResourceName;
    }

    @Override
    public boolean isIdleNow() {
        return counter.get() == 0;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        this.resourceCallback = callback;
    }

    public void increment() {
        counter.getAndIncrement();
    }

    public void decrement() {
        int counterVal = counter.decrementAndGet();
        if(counterVal == 0) {
            if(null != resourceCallback) {
                resourceCallback.onTransitionToIdle();
            }
        }

        if(counterVal < 0) {
            throw new IllegalArgumentException("Counter has been corrupted");
        }
    }
}
