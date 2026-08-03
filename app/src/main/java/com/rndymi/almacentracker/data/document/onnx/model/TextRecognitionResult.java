package com.rndymi.almacentracker.data.document.onnx.model;

import java.util.Objects;

public final class TextRecognitionResult {

    private final String text;
    private final float confidence;
    private final DetectedTextRegion sourceRegion;
    private final long preprocessDurationMs;
    private final long inferenceDurationMs;
    private final long decodeDurationMs;

    public TextRecognitionResult(
            String text,
            float confidence,
            DetectedTextRegion sourceRegion,
            long preprocessDurationMs,
            long inferenceDurationMs,
            long decodeDurationMs
    ) {
        this.text = Objects.requireNonNull(
                text,
                "text"
        );

        if (!Float.isFinite(confidence)
                || confidence < 0.0f
                || confidence > 1.0f) {
            throw new IllegalArgumentException(
                    "Recognition confidence must be between 0 and 1"
            );
        }

        this.sourceRegion = Objects.requireNonNull(
                sourceRegion,
                "sourceRegion"
        );

        requireNonNegative(
                preprocessDurationMs,
                "preprocessDurationMs"
        );
        requireNonNegative(
                inferenceDurationMs,
                "inferenceDurationMs"
        );
        requireNonNegative(
                decodeDurationMs,
                "decodeDurationMs"
        );

        this.confidence = confidence;
        this.preprocessDurationMs =
                preprocessDurationMs;
        this.inferenceDurationMs =
                inferenceDurationMs;
        this.decodeDurationMs =
                decodeDurationMs;
    }

    public String getText() {
        return text;
    }

    public float getConfidence() {
        return confidence;
    }

    public DetectedTextRegion getSourceRegion() {
        return sourceRegion;
    }

    public long getPreprocessDurationMs() {
        return preprocessDurationMs;
    }

    public long getInferenceDurationMs() {
        return inferenceDurationMs;
    }

    public long getDecodeDurationMs() {
        return decodeDurationMs;
    }

    public boolean isAboveThreshold(
            float threshold
    ) {
        if (!Float.isFinite(threshold)
                || threshold < 0.0f
                || threshold > 1.0f) {
            throw new IllegalArgumentException(
                    "Threshold must be between 0 and 1"
            );
        }

        return confidence >= threshold;
    }

    private static void requireNonNegative(
            long value,
            String fieldName
    ) {
        if (value < 0L) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }
}
