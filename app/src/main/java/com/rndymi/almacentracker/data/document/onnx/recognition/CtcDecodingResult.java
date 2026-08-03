package com.rndymi.almacentracker.data.document.onnx.recognition;

import java.util.Objects;

final class CtcDecodingResult {

    private final String text;
    private final float confidence;

    CtcDecodingResult(
            String text,
            float confidence
    ) {
        this.text = Objects.requireNonNull(
                text,
                "text"
        );

        if (!Float.isFinite(confidence)
                || confidence < 0.0f
                || confidence > 1.0f) {
            throw new IllegalArgumentException(
                    "Decoder confidence must be between 0 and 1"
            );
        }

        this.confidence = confidence;
    }

    String getText() {
        return text;
    }

    float getConfidence() {
        return confidence;
    }
}
