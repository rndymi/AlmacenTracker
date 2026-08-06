package com.rndymi.almacentracker.evaluation.metrics;

import java.util.Objects;

public final class EditDistanceResult {

    private final String expectedText;
    private final String actualText;
    private final int substitutions;
    private final int insertions;
    private final int deletions;
    private final int correctCharacters;
    private final double characterErrorRate;

    public EditDistanceResult(
            String expectedText,
            String actualText,
            int substitutions,
            int insertions,
            int deletions,
            int correctCharacters
    ) {
        this.expectedText =
                Objects.requireNonNull(
                        expectedText,
                        "expectedText"
                );

        this.actualText =
                Objects.requireNonNull(
                        actualText,
                        "actualText"
                );

        requireNonNegative(
                substitutions,
                "substitutions"
        );

        requireNonNegative(
                insertions,
                "insertions"
        );

        requireNonNegative(
                deletions,
                "deletions"
        );

        requireNonNegative(
                correctCharacters,
                "correctCharacters"
        );

        this.substitutions = substitutions;
        this.insertions = insertions;
        this.deletions = deletions;
        this.correctCharacters = correctCharacters;

        int expectedCharacterCount =
                expectedText.length();

        int totalErrors =
                substitutions
                        + insertions
                        + deletions;

        if (expectedCharacterCount == 0) {
            this.characterErrorRate =
                    actualText.isEmpty()
                            ? 0.0d
                            : 1.0d;
        } else {
            this.characterErrorRate =
                    (double) totalErrors
                            / (double) expectedCharacterCount;
        }
    }

    public String getExpectedText() {
        return expectedText;
    }

    public String getActualText() {
        return actualText;
    }

    public int getSubstitutions() {
        return substitutions;
    }

    public int getInsertions() {
        return insertions;
    }

    public int getDeletions() {
        return deletions;
    }

    public int getCorrectCharacters() {
        return correctCharacters;
    }

    public int getEditDistance() {
        return substitutions
                + insertions
                + deletions;
    }

    public double getCharacterErrorRate() {
        return characterErrorRate;
    }

    public boolean isExactMatch() {
        return expectedText.equals(actualText);
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
