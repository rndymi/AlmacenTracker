package com.rndymi.almacentracker.domain.reference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DocumentReferenceData {

    private final WarehouseReference reference;
    private final Integer quantity;
    private final List<Integer> quantitySuggestions;
    private final String unit;
    private final int sourceLineIndex;
    private final String sourceText;
    private final WarehouseReference observedReference;
    private final List<String> destinations;
    private final List<DocumentReferenceAllocation> allocations;

    public DocumentReferenceData(
            WarehouseReference reference,
            Integer quantity,
            String unit,
            int sourceLineIndex,
            String sourceText
    ) {
        this(
                reference,
                reference,
                quantity,
                unit,
                sourceLineIndex,
                sourceText,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public DocumentReferenceData(
            WarehouseReference reference,
            Integer quantity,
            String unit,
            int sourceLineIndex,
            String sourceText,
            List<Integer> quantitySuggestions
    ) {
        this(
                reference,
                reference,
                quantity,
                unit,
                sourceLineIndex,
                sourceText,
                quantitySuggestions,
                Collections.emptyList(),
                Collections.emptyList()
        );
    }

    public DocumentReferenceData(
            WarehouseReference reference,
            WarehouseReference observedReference,
            Integer quantity,
            String unit,
            int sourceLineIndex,
            String sourceText,
            List<Integer> quantitySuggestions,
            List<String> destinations
    ) {
        this(
                reference,
                observedReference,
                quantity,
                unit,
                sourceLineIndex,
                sourceText,
                quantitySuggestions,
                destinations,
                Collections.emptyList()
        );
    }

    public DocumentReferenceData(
            WarehouseReference reference,
            WarehouseReference observedReference,
            Integer quantity,
            String unit,
            int sourceLineIndex,
            String sourceText,
            List<Integer> quantitySuggestions,
            List<String> destinations,
            List<DocumentReferenceAllocation> allocations
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

        this.reference =
                Objects.requireNonNull(
                        reference,
                        "reference"
                );

        this.observedReference =
                observedReference == null
                        ? reference
                        : observedReference;

        this.quantity = quantity;

        this.quantitySuggestions =
                immutablePositiveQuantities(
                        quantitySuggestions,
                        quantity
                );

        this.unit = normalizeOptional(unit);

        this.sourceLineIndex = sourceLineIndex;

        this.sourceText = sourceText;

        this.destinations =
                immutableDestinations(
                        destinations
                );

        this.allocations =
                immutableAllocations(
                        allocations
                );
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public List<Integer> getQuantitySuggestions() {
        return quantitySuggestions;
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

    public WarehouseReference getObservedReference() {
        return observedReference;
    }

    public List<String> getDestinations() {
        return destinations;
    }

    public List<DocumentReferenceAllocation> getAllocations() {
        return allocations;
    }

    public boolean hasQuantityAmbiguity() {
        return !quantitySuggestions.isEmpty();
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

    private static List<Integer> immutablePositiveQuantities(
            List<Integer> values,
            Integer observedQuantity
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> result = new ArrayList<>();

        for (Integer value : values) {
            if (value != null
                    && value > 0
                    && !value.equals(observedQuantity)
                    && !result.contains(value)) {
                result.add(value);
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }

    private static List<String> immutableDestinations(
            List<String> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result =
                new ArrayList<>();

        for (String value : values) {
            String normalized =
                    normalizeOptional(value);

            if (normalized != null
                    && !result.contains(normalized)) {
                result.add(normalized);
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }

    private static List<DocumentReferenceAllocation>
    immutableAllocations(
            List<DocumentReferenceAllocation> values
    ) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyList();
        }

        List<DocumentReferenceAllocation> result =
                new ArrayList<>();

        for (DocumentReferenceAllocation value : values) {
            if (value != null
                    && !result.contains(value)) {
                result.add(value);
            }
        }

        return result.isEmpty()
                ? Collections.emptyList()
                : Collections.unmodifiableList(result);
    }
}
