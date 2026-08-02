package com.rndymi.almacentracker.data.document.onnx.detection;

import java.util.Objects;

public final class PaddleTextDetectorConfiguration {

    private static final String DEFAULT_INPUT_NAME = "x";
    private static final String DEFAULT_OUTPUT_NAME =
            "fetch_name_0";

    private final String inputName;
    private final String outputName;
    private final int maximumSide;
    private final int dimensionMultiple;
    private final float pixelThreshold;
    private final float boxThreshold;
    private final int minimumRegionSize;
    private final float unclipRatio;
    private final float duplicateIouThreshold;
    private final float lineOverlapThreshold;

    public PaddleTextDetectorConfiguration(
            String inputName,
            String outputName,
            int maximumSide,
            int dimensionMultiple,
            float pixelThreshold,
            float boxThreshold,
            int minimumRegionSize,
            float unclipRatio,
            float duplicateIouThreshold,
            float lineOverlapThreshold
    ) {
        this.inputName = requireName(
                inputName,
                "inputName"
        );
        this.outputName = requireName(
                outputName,
                "outputName"
        );

        if (maximumSide <= 0) {
            throw new IllegalArgumentException(
                    "maximumSide must be positive"
            );
        }

        if (dimensionMultiple <= 0) {
            throw new IllegalArgumentException(
                    "dimensionMultiple must be positive"
            );
        }

        requireProbability(
                pixelThreshold,
                "pixelThreshold"
        );
        requireProbability(
                boxThreshold,
                "boxThreshold"
        );
        requireProbability(
                duplicateIouThreshold,
                "duplicateIouThreshold"
        );
        requireProbability(
                lineOverlapThreshold,
                "lineOverlapThreshold"
        );

        if (minimumRegionSize <= 0) {
            throw new IllegalArgumentException(
                    "minimumRegionSize must be positive"
            );
        }

        if (!Float.isFinite(unclipRatio)
                || unclipRatio < 1.0f) {
            throw new IllegalArgumentException(
                    "unclipRatio must be finite and at least 1"
            );
        }

        this.maximumSide = maximumSide;
        this.dimensionMultiple = dimensionMultiple;
        this.pixelThreshold = pixelThreshold;
        this.boxThreshold = boxThreshold;
        this.minimumRegionSize = minimumRegionSize;
        this.unclipRatio = unclipRatio;
        this.duplicateIouThreshold =
                duplicateIouThreshold;
        this.lineOverlapThreshold =
                lineOverlapThreshold;
    }

    public static PaddleTextDetectorConfiguration
    defaultConfiguration() {
        return new PaddleTextDetectorConfiguration(
                DEFAULT_INPUT_NAME,
                DEFAULT_OUTPUT_NAME,
                960,
                32,
                0.30f,
                0.55f,
                3,
                1.50f,
                0.80f,
                0.50f
        );
    }

    public String getInputName() {
        return inputName;
    }

    public String getOutputName() {
        return outputName;
    }

    public int getMaximumSide() {
        return maximumSide;
    }

    public int getDimensionMultiple() {
        return dimensionMultiple;
    }

    public float getPixelThreshold() {
        return pixelThreshold;
    }

    public float getBoxThreshold() {
        return boxThreshold;
    }

    public int getMinimumRegionSize() {
        return minimumRegionSize;
    }

    public float getUnclipRatio() {
        return unclipRatio;
    }

    public float getDuplicateIouThreshold() {
        return duplicateIouThreshold;
    }

    public float getLineOverlapThreshold() {
        return lineOverlapThreshold;
    }

    private static String requireName(
            String value,
            String fieldName
    ) {
        String normalized = Objects.requireNonNull(
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
                    fieldName
                            + " must be between 0 and 1"
            );
        }
    }
}
