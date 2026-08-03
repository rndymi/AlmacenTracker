package com.rndymi.almacentracker.data.document.onnx.model;

import java.util.Objects;

public final class DetectedTextRegion {

    private final DetectedTextPolygon polygon;
    private final float confidence;
    private final int sourceOrder;

    public DetectedTextRegion(
            DetectedTextPolygon polygon,
            float confidence,
            int sourceOrder
    ) {
        this.polygon = Objects.requireNonNull(
                polygon,
                "polygon"
        );

        if (!Float.isFinite(confidence)
                || confidence < 0.0f
                || confidence > 1.0f) {
            throw new IllegalArgumentException(
                    "Detection confidence must be between 0 and 1"
            );
        }

        if (sourceOrder < 0) {
            throw new IllegalArgumentException(
                    "Source order cannot be negative"
            );
        }

        this.confidence = confidence;
        this.sourceOrder = sourceOrder;
    }

    public DetectedTextPolygon getPolygon() {
        return polygon;
    }

    public float getConfidence() {
        return confidence;
    }

    public int getSourceOrder() {
        return sourceOrder;
    }

    public float getLeft() {
        return polygon.getLeft();
    }

    public float getTop() {
        return polygon.getTop();
    }

    public float getRight() {
        return polygon.getRight();
    }

    public float getBottom() {
        return polygon.getBottom();
    }

    public float getWidth() {
        return polygon.getWidth();
    }

    public float getHeight() {
        return polygon.getHeight();
    }
}
