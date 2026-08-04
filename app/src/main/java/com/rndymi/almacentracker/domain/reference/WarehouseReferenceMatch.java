package com.rndymi.almacentracker.domain.reference;

import java.util.Objects;

public final class WarehouseReferenceMatch {

    private final WarehouseReference observedReference;
    private final WarehouseReference resolvedReference;
    private final int sourceLineIndex;
    private final String sourceRawText;
    private final int occurrenceIndex;

    public WarehouseReferenceMatch(
            WarehouseReference reference,
            int sourceLineIndex,
            String sourceRawText,
            int occurrenceIndex
    ) {
        this(
                reference,
                null,
                sourceLineIndex,
                sourceRawText,
                occurrenceIndex
        );
    }

    public WarehouseReferenceMatch(
            WarehouseReference observedReference,
            WarehouseReference resolvedReference,
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

        this.observedReference =
                Objects.requireNonNull(
                        observedReference,
                        "observedReference"
                );

        this.resolvedReference = resolvedReference;

        this.sourceLineIndex = sourceLineIndex;

        this.sourceRawText =
                Objects.requireNonNull(
                        sourceRawText,
                        "sourceRawText"
                );

        this.occurrenceIndex = occurrenceIndex;
    }

    public WarehouseReference getReference() {
        return resolvedReference != null
                ? resolvedReference
                : observedReference;
    }

    public WarehouseReference getObservedReference() {
        return observedReference;
    }

    public WarehouseReference getResolvedReference() {
        return resolvedReference;
    }

    public boolean hasResolvedReference() {
        return resolvedReference != null;
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

    public WarehouseReferenceMatch withResolvedReference(
            WarehouseReference reference
    ) {
        return new WarehouseReferenceMatch(
                observedReference,
                Objects.requireNonNull(
                        reference,
                        "reference"
                ),
                sourceLineIndex,
                sourceRawText,
                occurrenceIndex
        );
    }
}
