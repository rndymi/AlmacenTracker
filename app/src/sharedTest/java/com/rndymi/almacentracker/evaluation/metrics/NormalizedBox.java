package com.rndymi.almacentracker.evaluation.metrics;

import java.util.Objects;

public final class NormalizedBox {

    private final double left;
    private final double top;
    private final double right;
    private final double bottom;

    public NormalizedBox(
            double left,
            double top,
            double right,
            double bottom
    ) {
        requireCoordinate(left, "left");
        requireCoordinate(top, "top");
        requireCoordinate(right, "right");
        requireCoordinate(bottom, "bottom");

        if (right <= left) {
            throw new IllegalArgumentException(
                    "right must be greater than left"
            );
        }

        if (bottom <= top) {
            throw new IllegalArgumentException(
                    "bottom must be greater than top"
            );
        }

        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public double getLeft() {
        return left;
    }

    public double getTop() {
        return top;
    }

    public double getRight() {
        return right;
    }

    public double getBottom() {
        return bottom;
    }

    public double getArea() {
        return (right - left)
                * (bottom - top);
    }

    public double intersectionOverUnion(
            NormalizedBox other
    ) {
        Objects.requireNonNull(other, "other");

        double intersectionLeft =
                Math.max(left, other.left);

        double intersectionTop =
                Math.max(top, other.top);

        double intersectionRight =
                Math.min(right, other.right);

        double intersectionBottom =
                Math.min(bottom, other.bottom);

        double width = Math.max(
                0.0d,
                intersectionRight - intersectionLeft
        );

        double height = Math.max(
                0.0d,
                intersectionBottom - intersectionTop
        );

        double intersectionArea =
                width * height;

        if (intersectionArea == 0.0d) {
            return 0.0d;
        }

        double unionArea =
                getArea()
                        + other.getArea()
                        - intersectionArea;

        if (unionArea <= 0.0d) {
            return 0.0d;
        }

        return intersectionArea / unionArea;
    }

    public boolean overlaps(
            NormalizedBox other,
            double minimumIou
    ) {
        if (!Double.isFinite(minimumIou)
                || minimumIou < 0.0d
                || minimumIou > 1.0d) {
            throw new IllegalArgumentException(
                    "minimumIou must be between 0 and 1"
            );
        }

        return intersectionOverUnion(other)
                >= minimumIou;
    }

    private static void requireCoordinate(
            double value,
            String fieldName
    ) {
        if (!Double.isFinite(value)
                || value < 0.0d
                || value > 1.0d) {
            throw new IllegalArgumentException(
                    fieldName
                            + " must be between 0 and 1"
            );
        }
    }
}
