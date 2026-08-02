package com.rndymi.almacentracker.data.document.onnx;

import java.util.Objects;

public final class PaddleOcrInitializationResult {

    public enum Status {
        READY,
        ERROR
    }

    private final Status status;
    private final PaddleOcrInitializationError error;
    private final PaddleOcrSessionBundle sessionBundle;
    private final Throwable technicalCause;

    private PaddleOcrInitializationResult(
            Status status,
            PaddleOcrInitializationError error,
            PaddleOcrSessionBundle sessionBundle,
            Throwable technicalCause
    ) {
        this.status =
                Objects.requireNonNull(
                        status,
                        "status"
                );
        this.error =
                Objects.requireNonNull(
                        error,
                        "error"
                );
        this.sessionBundle = sessionBundle;
        this.technicalCause = technicalCause;
    }

    public static PaddleOcrInitializationResult ready(
            PaddleOcrSessionBundle sessionBundle
    ) {
        return new PaddleOcrInitializationResult(
                Status.READY,
                PaddleOcrInitializationError.NONE,
                Objects.requireNonNull(
                        sessionBundle,
                        "sessionBundle"
                ),
                null
        );
    }

    public static PaddleOcrInitializationResult error(
            PaddleOcrInitializationError error,
            Throwable technicalCause
    ) {
        if (error == PaddleOcrInitializationError.NONE) {
            throw new IllegalArgumentException(
                    "An error result requires an error code"
            );
        }

        return new PaddleOcrInitializationResult(
                Status.ERROR,
                error,
                null,
                technicalCause
        );
    }

    public Status getStatus() {
        return status;
    }

    public PaddleOcrInitializationError getError() {
        return error;
    }

    public PaddleOcrSessionBundle getSessionBundle() {
        return sessionBundle;
    }

    Throwable getTechnicalCause() {
        return technicalCause;
    }

    public boolean isReady() {
        return status == Status.READY;
    }
}
