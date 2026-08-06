package com.rndymi.almacentracker.evaluation.performance;

public final class MemorySnapshot {

    private final long javaUsedBytes;
    private final long nativeHeapBytes;
    private final long capturedAtNanos;

    public MemorySnapshot(
            long javaUsedBytes,
            long nativeHeapBytes,
            long capturedAtNanos
    ) {
        if (javaUsedBytes < 0L
                || nativeHeapBytes < 0L
                || capturedAtNanos < 0L) {
            throw new IllegalArgumentException(
                    "Memory snapshot values cannot be negative"
            );
        }

        this.javaUsedBytes = javaUsedBytes;
        this.nativeHeapBytes = nativeHeapBytes;
        this.capturedAtNanos = capturedAtNanos;
    }

    public long getJavaUsedBytes() {
        return javaUsedBytes;
    }

    public long getNativeHeapBytes() {
        return nativeHeapBytes;
    }

    public long getCapturedAtNanos() {
        return capturedAtNanos;
    }
}
