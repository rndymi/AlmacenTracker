package com.rndymi.almacentracker.core.document;

public interface DocumentImage extends AutoCloseable {
    int getOriginalWidth();
    int getOriginalHeight();
    int getAppliedRotation();
    int getProcessedWidth();
    int getProcessedHeight();
    boolean isClosed();

    @Override
    void close();
}
