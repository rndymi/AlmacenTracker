package com.rndymi.almacentracker.data.document.onnx;

import java.util.Objects;

final class PaddleOcrMetadataException
        extends IllegalArgumentException {

    private final PaddleOcrInitializationError error;

    PaddleOcrMetadataException(
            PaddleOcrInitializationError error,
            String message
    ) {
        super(message);

        this.error = Objects.requireNonNull(
                error,
                "error"
        );
    }

    PaddleOcrMetadataException(
            PaddleOcrInitializationError error,
            String message,
            Throwable cause
    ) {
        super(message, cause);

        this.error = Objects.requireNonNull(
                error,
                "error"
        );
    }

    PaddleOcrInitializationError getError() {
        return error;
    }
}
