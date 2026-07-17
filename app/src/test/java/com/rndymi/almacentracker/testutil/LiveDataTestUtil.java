package com.rndymi.almacentracker.testutil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class LiveDataTestUtil {
    private LiveDataTestUtil() {
    }

    public static <T> T getOrAwaitValue(
            LiveData<T> liveData
    ) throws InterruptedException {

        Object[] data = new Object[1];
        CountDownLatch latch = new CountDownLatch(1);

        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(T value) {
                data[0] = value;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };

        liveData.observeForever(observer);

        boolean completed = latch.await(
                2,
                TimeUnit.SECONDS
        );

        if (!completed) {
            liveData.removeObserver(observer);
            throw new AssertionError(
                    "LiveData value was never set"
            );
        }

        @SuppressWarnings("unchecked")
        T value = (T) data[0];

        return value;
    }
}
