package com.rndymi.almacentracker.data.document.onnx.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TextDetectionResult {

    private final List<DetectedTextRegion> regions;
    private final int sourceWidth;
    private final int sourceHeight;
    private final int inferenceWidth;
    private final int inferenceHeight;
    private final long preprocessDurationMs;
    private final long inferenceDurationMs;
    private final long postprocessDurationMs;

    public TextDetectionResult(
            List<DetectedTextRegion> regions,
            int sourceWidth,
            int sourceHeight,
            int inferenceWidth,
            int inferenceHeight,
            long preprocessDurationMs,
            long inferenceDurationMs,
            long postprocessDurationMs
    ) {
        Objects.requireNonNull(regions, "regions");

        if (sourceWidth <= 0
                || sourceHeight <= 0
                || inferenceWidth <= 0
                || inferenceHeight <= 0) {
            throw new IllegalArgumentException(
                    "Detection dimensions must be positive"
            );
        }

        if (preprocessDurationMs < 0
                || inferenceDurationMs < 0
                || postprocessDurationMs < 0) {
            throw new IllegalArgumentException(
                    "Detection durations cannot be negative"
            );
        }

        ArrayList<DetectedTextRegion> copiedRegions =
                new ArrayList<>(regions.size());

        for (DetectedTextRegion region : regions) {
            DetectedTextRegion nonNullRegion =
                    Objects.requireNonNull(
                            region,
                            "regions cannot contain null values"
                    );

            validateRegionWithinSource(
                    nonNullRegion,
                    sourceWidth,
                    sourceHeight
            );

            copiedRegions.add(nonNullRegion);
        }

        this.regions = Collections.unmodifiableList(
                copiedRegions
        );
        this.sourceWidth = sourceWidth;
        this.sourceHeight = sourceHeight;
        this.inferenceWidth = inferenceWidth;
        this.inferenceHeight = inferenceHeight;
        this.preprocessDurationMs = preprocessDurationMs;
        this.inferenceDurationMs = inferenceDurationMs;
        this.postprocessDurationMs = postprocessDurationMs;
    }

    public List<DetectedTextRegion> getRegions() {
        return regions;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public int getInferenceWidth() {
        return inferenceWidth;
    }

    public int getInferenceHeight() {
        return inferenceHeight;
    }

    public long getPreprocessDurationMs() {
        return preprocessDurationMs;
    }

    public long getInferenceDurationMs() {
        return inferenceDurationMs;
    }

    public long getPostprocessDurationMs() {
        return postprocessDurationMs;
    }

    public long getTotalDurationMs() {
        return preprocessDurationMs
                + inferenceDurationMs
                + postprocessDurationMs;
    }

    private void validateRegionWithinSource(
            DetectedTextRegion region,
            int width,
            int height
    ) {
        if (region.getLeft() < 0.0f
                || region.getTop() < 0.0f
                || region.getRight() > width
                || region.getBottom() > height) {
            throw new IllegalArgumentException(
                    "Detected region is outside source bounds"
            );
        }
    }
}
