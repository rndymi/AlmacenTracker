package com.rndymi.almacentracker.data.document.onnx.model;

import java.util.Objects;

public final class DetectedTextPolygon {

    private final TextPoint topLeft;
    private final TextPoint topRight;
    private final TextPoint bottomRight;
    private final TextPoint bottomLeft;

    public DetectedTextPolygon(
            TextPoint topLeft,
            TextPoint topRight,
            TextPoint bottomRight,
            TextPoint bottomLeft
    ) {
        this.topLeft = Objects.requireNonNull(
                topLeft,
                "topLeft"
        );
        this.topRight = Objects.requireNonNull(
                topRight,
                "topRight"
        );
        this.bottomRight = Objects.requireNonNull(
                bottomRight,
                "bottomRight"
        );
        this.bottomLeft = Objects.requireNonNull(
                bottomLeft,
                "bottomLeft"
        );

        validateBounds();
    }

    public static DetectedTextPolygon rectangle(
            float left,
            float top,
            float right,
            float bottom
    ) {
        if (!Float.isFinite(left)
                || !Float.isFinite(top)
                || !Float.isFinite(right)
                || !Float.isFinite(bottom)) {
            throw new IllegalArgumentException(
                    "Polygon bounds must be finite"
            );
        }

        if (right <= left || bottom <= top) {
            throw new IllegalArgumentException(
                    "Polygon bounds must define a positive area"
            );
        }

        return new DetectedTextPolygon(
                new TextPoint(left, top),
                new TextPoint(right, top),
                new TextPoint(right, bottom),
                new TextPoint(left, bottom)
        );
    }

    public TextPoint getTopLeft() {
        return topLeft;
    }

    public TextPoint getTopRight() {
        return topRight;
    }

    public TextPoint getBottomRight() {
        return bottomRight;
    }

    public TextPoint getBottomLeft() {
        return bottomLeft;
    }

    public float getLeft() {
        return Math.min(
                Math.min(topLeft.getX(), topRight.getX()),
                Math.min(bottomLeft.getX(), bottomRight.getX())
        );
    }

    public float getTop() {
        return Math.min(
                Math.min(topLeft.getY(), topRight.getY()),
                Math.min(bottomLeft.getY(), bottomRight.getY())
        );
    }

    public float getRight() {
        return Math.max(
                Math.max(topLeft.getX(), topRight.getX()),
                Math.max(bottomLeft.getX(), bottomRight.getX())
        );
    }

    public float getBottom() {
        return Math.max(
                Math.max(topLeft.getY(), topRight.getY()),
                Math.max(bottomLeft.getY(), bottomRight.getY())
        );
    }

    public float getWidth() {
        return getRight() - getLeft();
    }

    public float getHeight() {
        return getBottom() - getTop();
    }

    public float getArea() {
        return getWidth() * getHeight();
    }

    private void validateBounds() {
        if (getWidth() <= 0.0f || getHeight() <= 0.0f) {
            throw new IllegalArgumentException(
                    "Detected polygon must define a positive area"
            );
        }
    }
}
