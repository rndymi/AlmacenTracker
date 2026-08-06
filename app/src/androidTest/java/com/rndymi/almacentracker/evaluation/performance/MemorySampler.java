package com.rndymi.almacentracker.evaluation.performance;

import android.os.Debug;
import android.os.SystemClock;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class MemorySampler
        implements AutoCloseable {

    private static final long DEFAULT_INTERVAL_MS = 50L;

    private final AtomicBoolean running =
            new AtomicBoolean(false);

    private final AtomicReference<MemorySnapshot> peak =
            new AtomicReference<>();

    private final long sampleIntervalMs;

    private Thread worker;

    public MemorySampler() {
        this(DEFAULT_INTERVAL_MS);
    }

    public MemorySampler(
            long sampleIntervalMs
    ) {
        if (sampleIntervalMs < 10L) {
            throw new IllegalArgumentException(
                    "sampleIntervalMs must be at least 10"
            );
        }

        this.sampleIntervalMs =
                sampleIntervalMs;
    }

    public MemorySnapshot capture() {
        Runtime runtime =
                Runtime.getRuntime();

        long javaUsedBytes =
                runtime.totalMemory()
                        - runtime.freeMemory();

        long nativeHeapBytes =
                Debug.getNativeHeapAllocatedSize();

        return new MemorySnapshot(
                javaUsedBytes,
                nativeHeapBytes,
                SystemClock.elapsedRealtimeNanos()
        );
    }

    public void start() {
        if (!running.compareAndSet(
                false,
                true
        )) {
            throw new IllegalStateException(
                    "Memory sampler is already running"
            );
        }

        peak.set(capture());

        worker = new Thread(
                this::sampleLoop,
                "ocr-evaluation-memory-sampler"
        );

        worker.start();
    }

    public MemorySnapshot getPeak() {
        MemorySnapshot result = peak.get();

        if (result == null) {
            throw new IllegalStateException(
                    "Memory sampler has not captured a value"
            );
        }

        return result;
    }

    private void sampleLoop() {
        while (running.get()) {
            updatePeak(capture());

            SystemClock.sleep(
                    sampleIntervalMs
            );
        }

        updatePeak(capture());
    }

    private void updatePeak(
            MemorySnapshot candidate
    ) {
        while (true) {
            MemorySnapshot current = peak.get();

            if (current != null
                    && total(current)
                    >= total(candidate)) {
                return;
            }

            if (peak.compareAndSet(
                    current,
                    candidate
            )) {
                return;
            }
        }
    }

    private long total(
            MemorySnapshot value
    ) {
        return value.getJavaUsedBytes()
                + value.getNativeHeapBytes();
    }

    @Override
    public void close() {
        running.set(false);

        Thread currentWorker = worker;

        if (currentWorker == null) {
            return;
        }

        try {
            currentWorker.join(2_000L);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }

        worker = null;
    }
}
