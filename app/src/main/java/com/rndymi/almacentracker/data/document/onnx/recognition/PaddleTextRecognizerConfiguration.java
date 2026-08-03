package com.rndymi.almacentracker.data.document.onnx.recognition;

import com.rndymi.almacentracker.data.document.onnx.PaddleOcrModelManifest;

import java.util.Objects;

public final class PaddleTextRecognizerConfiguration {

    private final String inputName;
    private final String outputName;
    private final int fixedHeight;
    private final int minimumWidth;
    private final int maximumWidth;
    private final int widthMultiple;
    private final int blankIndex;
    private final int classCount;
    private final int additionalSpecialTokenCount;
    private final float recognitionThreshold;
    private final float verticalAspectThreshold;

    public PaddleTextRecognizerConfiguration(
            String inputName,
            String outputName,
            int fixedHeight,
            int minimumWidth,
            int maximumWidth,
            int widthMultiple,
            int blankIndex,
            int classCount,
            int additionalSpecialTokenCount,
            float recognitionThreshold,
            float verticalAspectThreshold
    ) {
        this.inputName =
                requireName(inputName, "inputName");
        this.outputName =
                requireName(outputName, "outputName");

        if (fixedHeight <= 0) {
            throw new IllegalArgumentException(
                    "fixedHeight must be positive"
            );
        }

        if (minimumWidth <= 0) {
            throw new IllegalArgumentException(
                    "minimumWidth must be positive"
            );
        }

        if (maximumWidth < minimumWidth) {
            throw new IllegalArgumentException(
                    "maximumWidth must not be smaller "
                            + "than minimumWidth"
            );
        }

        if (widthMultiple <= 0) {
            throw new IllegalArgumentException(
                    "widthMultiple must be positive"
            );
        }

        if (blankIndex < 0 || blankIndex >= classCount) {
            throw new IllegalArgumentException(
                    "blankIndex is outside the class range"
            );
        }

        if (classCount <= 0) {
            throw new IllegalArgumentException(
                    "classCount must be positive"
            );
        }

        if (additionalSpecialTokenCount < 0) {
            throw new IllegalArgumentException(
                    "additionalSpecialTokenCount cannot be negative"
            );
        }

        requireProbability(
                recognitionThreshold,
                "recognitionThreshold"
        );

        if (!Float.isFinite(verticalAspectThreshold)
                || verticalAspectThreshold <= 1.0f) {
            throw new IllegalArgumentException(
                    "verticalAspectThreshold must be greater than 1"
            );
        }

        this.fixedHeight = fixedHeight;
        this.minimumWidth = minimumWidth;
        this.maximumWidth = maximumWidth;
        this.widthMultiple = widthMultiple;
        this.blankIndex = blankIndex;
        this.classCount = classCount;
        this.additionalSpecialTokenCount =
                additionalSpecialTokenCount;
        this.recognitionThreshold =
                recognitionThreshold;
        this.verticalAspectThreshold =
                verticalAspectThreshold;
    }

    public static PaddleTextRecognizerConfiguration from(
            PaddleOcrModelManifest manifest
    ) {
        Objects.requireNonNull(manifest, "manifest");

        return new PaddleTextRecognizerConfiguration(
                manifest.getRecognizerInputName(),
                manifest.getRecognizerOutputName(),
                manifest.getRecognizerFixedHeight(),
                32,
                2048,
                8,
                manifest.getRecognizerBlankIndex(),
                manifest.getRecognizerClassCount(),
                manifest.getAdditionalSpecialTokenCount(),
                0.45f,
                2.50f
        );
    }

    public String getInputName() {
        return inputName;
    }

    public String getOutputName() {
        return outputName;
    }

    public int getFixedHeight() {
        return fixedHeight;
    }

    public int getMinimumWidth() {
        return minimumWidth;
    }

    public int getMaximumWidth() {
        return maximumWidth;
    }

    public int getWidthMultiple() {
        return widthMultiple;
    }

    public int getBlankIndex() {
        return blankIndex;
    }

    public int getClassCount() {
        return classCount;
    }

    public int getAdditionalSpecialTokenCount() {
        return additionalSpecialTokenCount;
    }

    public float getRecognitionThreshold() {
        return recognitionThreshold;
    }

    public float getVerticalAspectThreshold() {
        return verticalAspectThreshold;
    }

    private static String requireName(
            String value,
            String fieldName
    ) {
        String normalized =
                Objects.requireNonNull(
                        value,
                        fieldName
                ).trim();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }

        return normalized;
    }

    private static void requireProbability(
            float value,
            String fieldName
    ) {
        if (!Float.isFinite(value)
                || value < 0.0f
                || value > 1.0f) {
            throw new IllegalArgumentException(
                    fieldName + " must be between 0 and 1"
            );
        }
    }
}
