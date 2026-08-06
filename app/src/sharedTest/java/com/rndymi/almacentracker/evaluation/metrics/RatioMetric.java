package com.rndymi.almacentracker.evaluation.metrics;

import java.util.Objects;

public final class RatioMetric {

    private final long numerator;
    private final long denominator;

    public RatioMetric(
            long numerator,
            long denominator
    ) {
        if (numerator < 0L) {
            throw new IllegalArgumentException(
                    "numerator cannot be negative"
            );
        }

        if (denominator < 0L) {
            throw new IllegalArgumentException(
                    "denominator cannot be negative"
            );
        }

        if (numerator > denominator
                && denominator > 0L) {
            throw new IllegalArgumentException(
                    "numerator cannot exceed denominator"
            );
        }

        this.numerator = numerator;
        this.denominator = denominator;
    }

    public long getNumerator() {
        return numerator;
    }

    public long getDenominator() {
        return denominator;
    }

    public boolean isDefined() {
        return denominator > 0L;
    }

    public double getValueOrZero() {
        if (!isDefined()) {
            return 0.0d;
        }

        return (double) numerator
                / (double) denominator;
    }

    public double getPercentageOrZero() {
        return getValueOrZero() * 100.0d;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RatioMetric)) {
            return false;
        }

        RatioMetric that = (RatioMetric) other;

        return numerator == that.numerator
                && denominator == that.denominator;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                numerator,
                denominator
        );
    }
}
