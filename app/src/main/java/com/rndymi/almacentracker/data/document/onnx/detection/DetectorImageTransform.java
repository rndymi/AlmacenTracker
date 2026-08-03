package com.rndymi.almacentracker.data.document.onnx.detection;

import com.rndymi.almacentracker.data.document.onnx.model.DetectedTextPolygon;
import com.rndymi.almacentracker.data.document.onnx.model.TextPoint;

public final class DetectorImageTransform {

    private final int sourceWidth;
    private final int sourceHeight;
    private final int resizedWidth;
    private final int resizedHeight;
    private final int paddedWidth;
    private final int paddedHeight;
    private final int paddingLeft;
    private final int paddingTop;
    private final int paddingRight;
    private final int paddingBottom;
    private final float scaleX;
    private final float scaleY;

    public DetectorImageTransform(
            int sourceWidth,
            int sourceHeight,
            int resizedWidth,
            int resizedHeight,
            int paddedWidth,
            int paddedHeight,
            int paddingLeft,
            int paddingTop,
            int paddingRight,
            int paddingBottom
    ) {
        requirePositive(sourceWidth, "sourceWidth");
        requirePositive(sourceHeight, "sourceHeight");
        requirePositive(resizedWidth, "resizedWidth");
        requirePositive(resizedHeight, "resizedHeight");
        requirePositive(paddedWidth, "paddedWidth");
        requirePositive(paddedHeight, "paddedHeight");

        requireNonNegative(paddingLeft, "paddingLeft");
        requireNonNegative(paddingTop, "paddingTop");
        requireNonNegative(paddingRight, "paddingRight");
        requireNonNegative(paddingBottom, "paddingBottom");

        if (resizedWidth + paddingLeft + paddingRight
                != paddedWidth) {
            throw new IllegalArgumentException(
                    "Horizontal dimensions are inconsistent"
            );
        }

        if (resizedHeight + paddingTop + paddingBottom
                != paddedHeight) {
            throw new IllegalArgumentException(
                    "Vertical dimensions are inconsistent"
            );
        }

        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.resizedWidth = resizedWidth;
        this.resizedHeight = resizedHeight;
        this.paddedWidth = paddedWidth;
        this.paddedHeight = paddedHeight;
        this.paddingLeft = paddingLeft;
        this.paddingTop = paddingTop;
        this.paddingRight = paddingRight;
        this.paddingBottom = paddingBottom;
        this.scaleX = (float) resizedWidth / sourceWidth;
        this.scaleY = (float) resizedHeight / sourceHeight;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public int getResizedWidth() {
        return resizedWidth;
    }

    public int getResizedHeight() {
        return resizedHeight;
    }

    public int getPaddedWidth() {
        return paddedWidth;
    }

    public int getPaddedHeight() {
        return paddedHeight;
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public float getScaleX() {
        return scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public boolean isInsideContent(
            float inferenceX,
            float inferenceY
    ) {
        return inferenceX >= paddingLeft
                && inferenceX <= paddingLeft + resizedWidth
                && inferenceY >= paddingTop
                && inferenceY <= paddingTop + resizedHeight;
    }

    public TextPoint restorePoint(
            float inferenceX,
            float inferenceY
    ) {
        float contentX = clamp(
                inferenceX - paddingLeft,
                0.0f,
                resizedWidth
        );
        float contentY = clamp(
                inferenceY - paddingTop,
                0.0f,
                resizedHeight
        );

        float sourceX = clamp(
                contentX / scaleX,
                0.0f,
                sourceWidth
        );
        float sourceY = clamp(
                contentY / scaleY,
                0.0f,
                sourceHeight
        );

        return new TextPoint(sourceX, sourceY);
    }

    public DetectedTextPolygon restoreRectangle(
            float left,
            float top,
            float right,
            float bottom
    ) {
        TextPoint restoredTopLeft =
                restorePoint(left, top);
        TextPoint restoredBottomRight =
                restorePoint(right, bottom);

        float restoredLeft = restoredTopLeft.getX();
        float restoredTop = restoredTopLeft.getY();
        float restoredRight =
                restoredBottomRight.getX();
        float restoredBottom =
                restoredBottomRight.getY();

        if (restoredRight <= restoredLeft) {
            restoredRight = Math.min(
                    sourceWidth,
                    restoredLeft + 1.0f
            );
        }

        if (restoredBottom <= restoredTop) {
            restoredBottom = Math.min(
                    sourceHeight,
                    restoredTop + 1.0f
            );
        }

        return DetectedTextPolygon.rectangle(
                restoredLeft,
                restoredTop,
                restoredRight,
                restoredBottom
        );
    }

    public float mapOutputXToInference(
            float outputX,
            int outputWidth
    ) {
        if (outputWidth <= 0) {
            throw new IllegalArgumentException(
                    "outputWidth must be positive"
            );
        }

        return outputX * paddedWidth / outputWidth;
    }

    public float mapOutputYToInference(
            float outputY,
            int outputHeight
    ) {
        if (outputHeight <= 0) {
            throw new IllegalArgumentException(
                    "outputHeight must be positive"
            );
        }

        return outputY * paddedHeight / outputHeight;
    }

    private static float clamp(
            float value,
            float minimum,
            float maximum
    ) {
        return Math.max(
                minimum,
                Math.min(maximum, value)
        );
    }

    private static void requirePositive(
            int value,
            String fieldName
    ) {
        if (value <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be positive"
            );
        }
    }

    private static void requireNonNegative(
            int value,
            String fieldName
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }
    }
}
