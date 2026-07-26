package com.rndymi.almacentracker.domain.reference;

import java.util.Objects;

public final class WarehouseReferenceMatch {

    private final WarehouseReference reference;
    private final int sourceLineIndex;
    private final String sourceRawText;
    private final int occurrenceIndex;

    public WarehouseReferenceMatch(
            WarehouseReference reference,
            int sourceLineIndex,
            String sourceRawText,
            int occurrenceIndex
    ) {
        if (sourceLineIndex < 0) {
            throw new IllegalArgumentException(
                    "sourceLineIndex cannot be negative"
            );
        }

        if (occurrenceIndex < 0) {
            throw new IllegalArgumentException(
                    "occurrenceIndex cannot be negative"
            );
        }

        this.reference =
                Objects.requireNonNull(
                        reference,
                        "reference"
                );

        this.sourceLineIndex =
                sourceLineIndex;

        this.sourceRawText =
                Objects.requireNonNull(
                        sourceRawText,
                        "sourceRawText"
                );

        this.occurrenceIndex =
                occurrenceIndex;
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public int getSourceLineIndex() {
        return sourceLineIndex;
    }

    public String getSourceRawText() {
        return sourceRawText;
    }

    public int getOccurrenceIndex() {
        return occurrenceIndex;
    }
}