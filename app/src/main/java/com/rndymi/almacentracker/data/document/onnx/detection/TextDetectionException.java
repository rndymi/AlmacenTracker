package com.rndymi.almacentracker.data.document.onnx.detection;

import java.util.Objects;

public final class TextDetectionException
        extends Exception {

    public enum Error {
        INVALID_IMAGE,
        RUNTIME_NOT_READY,
        SESSION_CLOSED,
        INPUT_SHAPE_INCOMPATIBLE,
        OUTPUT_NOT_FOUND,
        OUTPUT_TYPE_INCOMPATIBLE,
        OUTPUT_SHAPE_INCOMPATIBLE,
        OUTPUT_VALUE_INVALID,
        MEMORY_ERROR,
        INFERENCE_ERROR,
        POSTPROCESSING_ERROR
    }

    private final Error error;

    public TextDetectionException(
            Error error,
            String message
    ) {
        super(message);
        this.error = Objects.requireNonNull(
                error,
                "error"
        );
    }

    public TextDetectionException(
            Error error,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.error = Objects.requireNonNull(
                error,
                "error"
        );
    }

    public Error getError() {
        return error;
    }
}
