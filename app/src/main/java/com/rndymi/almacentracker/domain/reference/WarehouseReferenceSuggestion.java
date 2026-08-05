package com.rndymi.almacentracker.domain.reference;

import java.util.Objects;

public final class WarehouseReferenceSuggestion {

    private final WarehouseReference reference;
    private final int score;
    private final String explanation;

    public WarehouseReferenceSuggestion(
            WarehouseReference reference,
            int score,
            String explanation
    ) {
        if (score < 0) {
            throw new IllegalArgumentException(
                    "score cannot be negative"
            );
        }

        this.reference =
                Objects.requireNonNull(
                        reference,
                        "reference"
                );

        this.score = score;

        this.explanation =
                Objects.requireNonNull(
                        explanation,
                        "explanation"
                );
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public int getScore() {
        return score;
    }

    public String getExplanation() {
        return explanation;
    }
}
