package com.rndymi.almacentracker.data.document.onnx;

import ai.onnxruntime.OrtSession;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PaddleOcrSessionBundle
        implements AutoCloseable {

    private final OrtSession detectorSession;
    private final OrtSession recognizerSession;
    private final PaddleOcrDictionary dictionary;
    private final AtomicBoolean closed =
            new AtomicBoolean(false);

    public PaddleOcrSessionBundle(
            OrtSession detectorSession,
            OrtSession recognizerSession,
            PaddleOcrDictionary dictionary
    ) {
        this.detectorSession =
                Objects.requireNonNull(
                        detectorSession,
                        "detectorSession"
                );
        this.recognizerSession =
                Objects.requireNonNull(
                        recognizerSession,
                        "recognizerSession"
                );
        this.dictionary =
                Objects.requireNonNull(
                        dictionary,
                        "dictionary"
                );
    }

    public OrtSession getDetectorSession() {
        ensureOpen();
        return detectorSession;
    }

    public OrtSession getRecognizerSession() {
        ensureOpen();
        return recognizerSession;
    }

    public PaddleOcrDictionary getDictionary() {
        ensureOpen();
        return dictionary;
    }

    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        RuntimeException failure = null;

        try {
            recognizerSession.close();
        } catch (RuntimeException exception) {
            failure = exception;
        }

        try {
            detectorSession.close();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            } else {
                failure.addSuppressed(exception);
            }
        }

        if (failure != null) {
            throw failure;
        }
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "PP-OCRv5 sessions are closed"
            );
        }
    }
}
