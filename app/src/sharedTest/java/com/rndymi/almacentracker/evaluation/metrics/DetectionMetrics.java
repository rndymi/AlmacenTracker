package com.rndymi.almacentracker.evaluation.metrics;

import java.util.Objects;

public final class DetectionMetrics {

    private final int expectedRegionCount;
    private final int detectedRegionCount;
    private final int matchedRegionCount;
    private final int missedRegionCount;
    private final int extraRegionCount;
    private final int mergedRegionCount;
    private final int splitRegionCount;
    private final RatioMetric precision;
    private final RatioMetric recall;
    private final double f1;

    public DetectionMetrics(
            int expectedRegionCount,
            int detectedRegionCount,
            int matchedRegionCount,
            int missedRegionCount,
            int extraRegionCount,
            int mergedRegionCount,
            int splitRegionCount
    ) {
        requireNonNegative(
                expectedRegionCount,
                "expectedRegionCount"
        );
        requireNonNegative(
                detectedRegionCount,
                "detectedRegionCount"
        );
        requireNonNegative(
                matchedRegionCount,
                "matchedRegionCount"
        );
        requireNonNegative(
                missedRegionCount,
                "missedRegionCount"
        );
        requireNonNegative(
                extraRegionCount,
                "extraRegionCount"
        );
        requireNonNegative(
                mergedRegionCount,
                "mergedRegionCount"
        );
        requireNonNegative(
                splitRegionCount,
                "splitRegionCount"
        );

        if (matchedRegionCount > expectedRegionCount) {
            throw new IllegalArgumentException(
                    "matchedRegionCount cannot exceed expectedRegionCount"
            );
        }

        if (matchedRegionCount > detectedRegionCount) {
            throw new IllegalArgumentException(
                    "matchedRegionCount cannot exceed detectedRegionCount"
            );
        }

        if (missedRegionCount
                != expectedRegionCount - matchedRegionCount) {
            throw new IllegalArgumentException(
                    "missedRegionCount is inconsistent"
            );
        }

        if (extraRegionCount
                != detectedRegionCount - matchedRegionCount) {
            throw new IllegalArgumentException(
                    "extraRegionCount is inconsistent"
            );
        }

        this.expectedRegionCount = expectedRegionCount;
        this.detectedRegionCount = detectedRegionCount;
        this.matchedRegionCount = matchedRegionCount;
        this.missedRegionCount = missedRegionCount;
        this.extraRegionCount = extraRegionCount;
        this.mergedRegionCount = mergedRegionCount;
        this.splitRegionCount = splitRegionCount;

        this.precision = new RatioMetric(
                matchedRegionCount,
                detectedRegionCount
        );

        this.recall = new RatioMetric(
                matchedRegionCount,
                expectedRegionCount
        );

        double precisionValue =
                precision.getValueOrZero();

        double recallValue =
                recall.getValueOrZero();

        if (precisionValue == 0.0d
                && recallValue == 0.0d) {
            this.f1 = 0.0d;
        } else {
            this.f1 =
                    2.0d
                            * precisionValue
                            * recallValue
                            / (
                            precisionValue
                                    + recallValue
                    );
        }
    }

    public int getExpectedRegionCount() {
        return expectedRegionCount;
    }

    public int getDetectedRegionCount() {
        return detectedRegionCount;
    }

    public int getMatchedRegionCount() {
        return matchedRegionCount;
    }

    public int getMissedRegionCount() {
        return missedRegionCount;
    }

    public int getExtraRegionCount() {
        return extraRegionCount;
    }

    public int getMergedRegionCount() {
        return mergedRegionCount;
    }

    public int getSplitRegionCount() {
        return splitRegionCount;
    }

    public RatioMetric getPrecision() {
        return precision;
    }

    public RatioMetric getRecall() {
        return recall;
    }

    public double getF1() {
        return f1;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DetectionMetrics)) {
            return false;
        }

        DetectionMetrics that =
                (DetectionMetrics) other;

        return expectedRegionCount
                == that.expectedRegionCount
                && detectedRegionCount
                == that.detectedRegionCount
                && matchedRegionCount
                == that.matchedRegionCount
                && missedRegionCount
                == that.missedRegionCount
                && extraRegionCount
                == that.extraRegionCount
                && mergedRegionCount
                == that.mergedRegionCount
                && splitRegionCount
                == that.splitRegionCount
                && Double.compare(f1, that.f1) == 0
                && precision.equals(that.precision)
                && recall.equals(that.recall);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                expectedRegionCount,
                detectedRegionCount,
                matchedRegionCount,
                missedRegionCount,
                extraRegionCount,
                mergedRegionCount,
                splitRegionCount,
                precision,
                recall,
                f1
        );
    }
}
