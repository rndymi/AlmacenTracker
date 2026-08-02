package com.rndymi.almacentracker.domain.reference;

import java.util.Objects;

public final class DocumentReferenceData {

    private final WarehouseReference reference;
    private final Integer quantity;
    private final String unit;
    private final int sourceLineIndex;
    private final String sourceText;

    public DocumentReferenceData(
            WarehouseReference reference,
            Integer quantity,
            String unit,
            int sourceLineIndex,
            String sourceText
    ) {
        if (sourceLineIndex < 0) {
            throw new IllegalArgumentException(
                    "sourceLineIndex must not be negative"
            );
        }

        if (quantity != null && quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be positive"
            );
        }

        this.reference = Objects.requireNonNull(
                reference,
                "reference"
        );

        this.quantity = quantity;
        this.unit = normalizeOptional(unit);
        this.sourceLineIndex = sourceLineIndex;
        this.sourceText = sourceText;
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public int getSourceLineIndex() {
        return sourceLineIndex;
    }

    public String getSourceText() {
        return sourceText;
    }

    public boolean hasQuantityProposal() {
        return quantity != null;
    }

    public boolean hasUnitProposal() {
        return unit != null;
    }

    private static String normalizeOptional(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}