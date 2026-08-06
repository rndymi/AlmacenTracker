package com.rndymi.almacentracker.evaluation.metrics;

public final class ReconstructionMetrics {

    private final int expectedLineCount;
    private final int reconstructedLineCount;
    private final int exactLineMatches;
    private final int mergedLineCount;
    private final int splitLineCount;
    private final int correctOrderCount;
    private final int expectedGlobalLineCount;
    private final int correctGlobalLineCount;

    public ReconstructionMetrics(
            int expectedLineCount,
            int reconstructedLineCount,
            int exactLineMatches,
            int mergedLineCount,
            int splitLineCount,
            int correctOrderCount,
            int expectedGlobalLineCount,
            int correctGlobalLineCount
    ) {
        requireNonNegative(
                expectedLineCount,
                "expectedLineCount"
        );
        requireNonNegative(
                reconstructedLineCount,
                "reconstructedLineCount"
        );
        requireNonNegative(
                exactLineMatches,
                "exactLineMatches"
        );
        requireNonNegative(
                mergedLineCount,
                "mergedLineCount"
        );
        requireNonNegative(
                splitLineCount,
                "splitLineCount"
        );
        requireNonNegative(
                correctOrderCount,
                "correctOrderCount"
        );
        requireNonNegative(
                expectedGlobalLineCount,
                "expectedGlobalLineCount"
        );
        requireNonNegative(
                correctGlobalLineCount,
                "correctGlobalLineCount"
        );

        this.expectedLineCount = expectedLineCount;
        this.reconstructedLineCount =
                reconstructedLineCount;
        this.exactLineMatches = exactLineMatches;
        this.mergedLineCount = mergedLineCount;
        this.splitLineCount = splitLineCount;
        this.correctOrderCount = correctOrderCount;
        this.expectedGlobalLineCount =
                expectedGlobalLineCount;
        this.correctGlobalLineCount =
                correctGlobalLineCount;
    }

    public int getExpectedLineCount() {
        return expectedLineCount;
    }

    public int getReconstructedLineCount() {
        return reconstructedLineCount;
    }

    public int getExactLineMatches() {
        return exactLineMatches;
    }

    public int getMergedLineCount() {
        return mergedLineCount;
    }

    public int getSplitLineCount() {
        return splitLineCount;
    }

    public int getCorrectOrderCount() {
        return correctOrderCount;
    }

    public RatioMetric getExactLineRate() {
        return new RatioMetric(
                exactLineMatches,
                expectedLineCount
        );
    }

    public RatioMetric getColumnOrderAccuracy() {
        return new RatioMetric(
                correctOrderCount,
                expectedLineCount
        );
    }

    public RatioMetric getGlobalLineAccuracy() {
        return new RatioMetric(
                correctGlobalLineCount,
                expectedGlobalLineCount
        );
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
