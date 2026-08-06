package com.rndymi.almacentracker.evaluation.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class OcrExpectedReference {

    private final String category;
    private final String code;
    private final int sourceLineIndex;
    private final String expectedMatchStatus;
    private final String expectedQuantity;
    private final String expectedUnit;
    private final List<String> expectedDestinations;
    private final String observedOcrVariant;
    private final String expectedSuggestion;
    private final boolean knownInRoom;

    public OcrExpectedReference(
            String category,
            String code,
            int sourceLineIndex,
            String expectedMatchStatus,
            String expectedQuantity,
            String expectedUnit,
            List<String> expectedDestinations,
            String observedOcrVariant,
            String expectedSuggestion,
            boolean knownInRoom
    ) {
        this.category =
                requireText(
                        category,
                        "category"
                );
        this.code = requireText(code, "code");

        if (sourceLineIndex < 0) {
            throw new IllegalArgumentException(
                    "sourceLineIndex cannot be negative"
            );
        }

        this.sourceLineIndex =
                sourceLineIndex;

        this.expectedMatchStatus =
                requireText(
                        expectedMatchStatus,
                        "expectedMatchStatus"
                );

        this.expectedQuantity =
                normalizeNullable(expectedQuantity);

        this.expectedUnit =
                normalizeNullable(expectedUnit);

        Objects.requireNonNull(
                expectedDestinations,
                "expectedDestinations"
        );

        List<String> copiedDestinations =
                new ArrayList<>(
                        expectedDestinations.size()
                );

        for (String destination :
                expectedDestinations) {
            copiedDestinations.add(
                    requireText(
                            destination,
                            "destination"
                    )
            );
        }

        this.expectedDestinations =
                Collections.unmodifiableList(
                        copiedDestinations
                );

        this.observedOcrVariant =
                normalizeNullable(
                        observedOcrVariant
                );

        this.expectedSuggestion =
                normalizeNullable(
                        expectedSuggestion
                );

        this.knownInRoom = knownInRoom;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getIdentity() {
        return category + code;
    }

    public int getSourceLineIndex() {
        return sourceLineIndex;
    }

    public String getExpectedMatchStatus() {
        return expectedMatchStatus;
    }

    public String getExpectedQuantity() {
        return expectedQuantity;
    }

    public String getExpectedUnit() {
        return expectedUnit;
    }

    public List<String> getExpectedDestinations() {
        return expectedDestinations;
    }

    public String getObservedOcrVariant() {
        return observedOcrVariant;
    }

    public String getExpectedSuggestion() {
        return expectedSuggestion;
    }

    public boolean isKnownInRoom() {
        return knownInRoom;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        Objects.requireNonNull(value, fieldName);

        String trimmed = value.trim();

        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be blank"
            );
        }

        return trimmed;
    }

    private static String normalizeNullable(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();

        return trimmed.isEmpty()
                ? null
                : trimmed;
    }
}
