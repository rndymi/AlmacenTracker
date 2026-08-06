package com.rndymi.almacentracker.evaluation.metrics;

public final class InterpretationMetrics {

    private final int expectedReferenceCount;
    private final int proposedReferenceCount;
    private final int exactMatches;
    private final int roomRecoveredReferences;
    private final int uniqueSuggestions;
    private final int ambiguities;
    private final int noMatches;
    private final int falsePositiveReferences;
    private final int missedReferences;
    private final int correctQuantities;
    private final int expectedQuantities;
    private final int correctUnits;
    private final int expectedUnits;
    private final int correctDestinations;
    private final int expectedDestinations;
    private final int correctTitles;
    private final int expectedTitles;

    public InterpretationMetrics(
            int expectedReferenceCount,
            int proposedReferenceCount,
            int exactMatches,
            int roomRecoveredReferences,
            int uniqueSuggestions,
            int ambiguities,
            int noMatches,
            int falsePositiveReferences,
            int missedReferences,
            int correctQuantities,
            int expectedQuantities,
            int correctUnits,
            int expectedUnits,
            int correctDestinations,
            int expectedDestinations,
            int correctTitles,
            int expectedTitles
    ) {
        this.expectedReferenceCount =
                requireNonNegative(
                        expectedReferenceCount,
                        "expectedReferenceCount"
                );
        this.proposedReferenceCount =
                requireNonNegative(
                        proposedReferenceCount,
                        "proposedReferenceCount"
                );
        this.exactMatches =
                requireNonNegative(
                        exactMatches,
                        "exactMatches"
                );
        this.roomRecoveredReferences =
                requireNonNegative(
                        roomRecoveredReferences,
                        "roomRecoveredReferences"
                );
        this.uniqueSuggestions =
                requireNonNegative(
                        uniqueSuggestions,
                        "uniqueSuggestions"
                );
        this.ambiguities =
                requireNonNegative(
                        ambiguities,
                        "ambiguities"
                );
        this.noMatches =
                requireNonNegative(
                        noMatches,
                        "noMatches"
                );
        this.falsePositiveReferences =
                requireNonNegative(
                        falsePositiveReferences,
                        "falsePositiveReferences"
                );
        this.missedReferences =
                requireNonNegative(
                        missedReferences,
                        "missedReferences"
                );
        this.correctQuantities =
                requireNonNegative(
                        correctQuantities,
                        "correctQuantities"
                );
        this.expectedQuantities =
                requireNonNegative(
                        expectedQuantities,
                        "expectedQuantities"
                );
        this.correctUnits =
                requireNonNegative(
                        correctUnits,
                        "correctUnits"
                );
        this.expectedUnits =
                requireNonNegative(
                        expectedUnits,
                        "expectedUnits"
                );
        this.correctDestinations =
                requireNonNegative(
                        correctDestinations,
                        "correctDestinations"
                );
        this.expectedDestinations =
                requireNonNegative(
                        expectedDestinations,
                        "expectedDestinations"
                );
        this.correctTitles =
                requireNonNegative(
                        correctTitles,
                        "correctTitles"
                );
        this.expectedTitles =
                requireNonNegative(
                        expectedTitles,
                        "expectedTitles"
                );
    }

    public int getExpectedReferenceCount() {
        return expectedReferenceCount;
    }

    public int getProposedReferenceCount() {
        return proposedReferenceCount;
    }

    public int getExactMatches() {
        return exactMatches;
    }

    public int getRoomRecoveredReferences() {
        return roomRecoveredReferences;
    }

    public int getUniqueSuggestions() {
        return uniqueSuggestions;
    }

    public int getAmbiguities() {
        return ambiguities;
    }

    public int getNoMatches() {
        return noMatches;
    }

    public int getFalsePositiveReferences() {
        return falsePositiveReferences;
    }

    public int getMissedReferences() {
        return missedReferences;
    }

    public RatioMetric getExactReferenceRate() {
        return new RatioMetric(
                exactMatches,
                expectedReferenceCount
        );
    }

    public RatioMetric getRoomRecoveryRate() {
        return new RatioMetric(
                roomRecoveredReferences,
                expectedReferenceCount
        );
    }

    public RatioMetric getFalsePositiveRate() {
        return new RatioMetric(
                falsePositiveReferences,
                proposedReferenceCount
        );
    }

    public RatioMetric getMissedReferenceRate() {
        return new RatioMetric(
                missedReferences,
                expectedReferenceCount
        );
    }

    public RatioMetric getQuantityAccuracy() {
        return new RatioMetric(
                correctQuantities,
                expectedQuantities
        );
    }

    public RatioMetric getUnitAccuracy() {
        return new RatioMetric(
                correctUnits,
                expectedUnits
        );
    }

    public RatioMetric getDestinationAccuracy() {
        return new RatioMetric(
                correctDestinations,
                expectedDestinations
        );
    }

    public RatioMetric getTitleAccuracy() {
        return new RatioMetric(
                correctTitles,
                expectedTitles
        );
    }

    private static int requireNonNegative(
            int value,
            String fieldName
    ) {
        if (value < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative"
            );
        }

        return value;
    }
}
