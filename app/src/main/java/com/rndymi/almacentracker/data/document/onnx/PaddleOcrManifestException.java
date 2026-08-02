package com.rndymi.almacentracker.data.document.onnx;

final class PaddleOcrManifestException
        extends IllegalArgumentException {

    PaddleOcrManifestException(String message) {
        super(message);
    }

    PaddleOcrManifestException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
