package com.rndymi.almacentracker.data.document.onnx.recognition;

public final class TextRecognitionException
        extends Exception {

    public enum Error {
        INVALID_IMAGE,
        INVALID_REGION,
        RUNTIME_NOT_READY,
        SESSION_CLOSED,
        INPUT_SHAPE_INCOMPATIBLE,
        OUTPUT_NOT_FOUND,
        OUTPUT_SHAPE_INCOMPATIBLE,
        CLASS_COUNT_MISMATCH,
        DICTIONARY_MISMATCH,
        INVALID_CLASS_INDEX,
        PREPROCESSING_ERROR,
        INFERENCE_ERROR,
        DECODING_ERROR,
        MEMORY_ERROR
    }

    private final Error error;

    public TextRecognitionException(
            Error error,
            String message
    ) {
        super(message);
        this.error = requireError(error);
    }

    public TextRecognitionException(
            Error error,
            String message,
            Throwable cause
    ) {
        super(message, cause);
        this.error = requireError(error);
    }

    public Error getError() {
        return error;
    }

    private static Error requireError(
            Error value
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    "Recognition error cannot be null"
            );
        }

        return value;
    }
}
