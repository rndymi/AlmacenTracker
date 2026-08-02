package com.rndymi.almacentracker.data.document.onnx;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

public final class PaddleOcrRuntimeProvider
        implements AutoCloseable {

    public enum State {
        NOT_INITIALIZED,
        INITIALIZING,
        READY,
        ERROR,
        CLOSED
    }

    public interface Callback {
        void onResult(
                PaddleOcrInitializationResult result
        );
    }

    private final Object lock = new Object();
    private final ExecutorService executorService;
    private final PaddleOcrRuntimeFactory initializer;

    private final List<Callback> pendingCallbacks =
            new ArrayList<>();

    private State state = State.NOT_INITIALIZED;
    private PaddleOcrInitializationResult result;

    public PaddleOcrRuntimeProvider(
            ExecutorService executorService,
            PaddleOcrRuntimeFactory initializer
    ) {
        this.executorService =
                Objects.requireNonNull(
                        executorService,
                        "executorService"
                );
        this.initializer =
                Objects.requireNonNull(
                        initializer,
                        "initializer"
                );
    }

    public State getState() {
        synchronized (lock) {
            return state;
        }
    }

    public void initialize(
            Callback callback
    ) {
        Objects.requireNonNull(
                callback,
                "callback"
        );

        PaddleOcrInitializationResult immediateResult =
                null;
        boolean shouldInitialize = false;

        synchronized (lock) {
            if (state == State.CLOSED) {
                immediateResult =
                        PaddleOcrInitializationResult.error(
                                PaddleOcrInitializationError
                                        .PROVIDER_CLOSED,
                                null
                        );
            } else if (state == State.READY) {
                immediateResult = result;
            } else if (state == State.INITIALIZING) {
                pendingCallbacks.add(callback);
            } else {
                state = State.INITIALIZING;
                pendingCallbacks.add(callback);
                shouldInitialize = true;
            }
        }

        if (immediateResult != null) {
            callback.onResult(immediateResult);
            return;
        }

        if (shouldInitialize) {
            executorService.execute(
                    this::performInitialization
            );
        }
    }

    private void performInitialization() {
        PaddleOcrInitializationResult initialized =
                initializer.initialize();

        List<Callback> callbacks;

        synchronized (lock) {
            if (state == State.CLOSED) {
                closeResultBundle(initialized);
                pendingCallbacks.clear();
                return;
            }

            result = initialized;
            state = initialized.isReady()
                    ? State.READY
                    : State.ERROR;

            callbacks =
                    new ArrayList<>(pendingCallbacks);
            pendingCallbacks.clear();
        }

        for (Callback callback : callbacks) {
            callback.onResult(initialized);
        }
    }

    public PaddleOcrSessionBundle requireReadySessions() {
        synchronized (lock) {
            if (state != State.READY
                    || result == null
                    || result.getSessionBundle() == null) {
                throw new IllegalStateException(
                        "PP-OCRv5 runtime is not ready"
                );
            }

            return result.getSessionBundle();
        }
    }

    @Override
    public void close() {
        PaddleOcrInitializationResult currentResult;

        synchronized (lock) {
            if (state == State.CLOSED) {
                return;
            }

            state = State.CLOSED;
            currentResult = result;
            result = null;
            pendingCallbacks.clear();
        }

        closeResultBundle(currentResult);
    }

    private void closeResultBundle(
            PaddleOcrInitializationResult value
    ) {
        if (value == null
                || value.getSessionBundle() == null) {
            return;
        }

        value.getSessionBundle().close();
    }
}
