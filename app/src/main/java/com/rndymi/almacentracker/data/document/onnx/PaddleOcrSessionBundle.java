package com.rndymi.almacentracker.data.document.onnx;

import ai.onnxruntime.OrtException;
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
        } catch (OrtException exception) {
            failure = closeFailure(exception);
        } catch (RuntimeException exception) {
            failure = exception;
        }

        try {
            detectorSession.close();
        } catch (OrtException exception) {
            RuntimeException detectorFailure =
                    closeFailure(exception);

            if (failure == null) {
                failure = detectorFailure;
            } else {
                failure.addSuppressed(detectorFailure);
            }
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

    private RuntimeException closeFailure(
            OrtException exception
    ) {
        return new IllegalStateException(
                "Unable to close PP-OCRv5 session",
                exception
        );
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException(
                    "PP-OCRv5 sessions are closed"
            );
        }
    }
}
