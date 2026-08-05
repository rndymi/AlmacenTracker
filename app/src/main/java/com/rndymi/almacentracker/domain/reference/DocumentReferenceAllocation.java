package com.rndymi.almacentracker.domain.reference;

import java.util.Locale;
import java.util.Objects;

public final class DocumentReferenceAllocation {

    private final int quantity;
    private final String unit;
    private final String destination;

    public DocumentReferenceAllocation(
            int quantity,
            String unit,
            String destination
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "quantity must be positive"
            );
        }

        this.quantity = quantity;
        this.unit = normalizeRequired(
                unit,
                "unit"
        ).toUpperCase(Locale.ROOT);
        this.destination = normalizeRequired(
                destination,
                "destination"
        );
    }

    public int getQuantity() {
        return quantity;
    }

    public String getUnit() {
        return unit;
    }

    public String getDestination() {
        return destination;
    }

    private static String normalizeRequired(
            String value,
            String fieldName
    ) {
        if (value == null
                || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be empty"
            );
        }

        return value.trim().replaceAll(
                "[\\p{Z}\\s]+",
                " "
        );
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof DocumentReferenceAllocation)) {
            return false;
        }

        DocumentReferenceAllocation that =
                (DocumentReferenceAllocation) other;

        return quantity == that.quantity
                && unit.equals(that.unit)
                && destination.equals(that.destination);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                quantity,
                unit,
                destination
        );
    }
}
