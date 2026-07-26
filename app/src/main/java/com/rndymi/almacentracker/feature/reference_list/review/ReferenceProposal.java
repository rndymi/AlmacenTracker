package com.rndymi.almacentracker.feature.reference_list.review;

import com.rndymi.almacentracker.domain.reference.WarehouseReference;

import java.util.Objects;

public final class ReferenceProposal {

    private final long id;
    private final WarehouseReference reference;
    private final String sourceRawText;
    private final boolean manuallyAdded;

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded
    ) {
        this.id = id;

        this.reference =
                Objects.requireNonNull(
                        reference,
                        "reference"
                );

        this.sourceRawText =
                sourceRawText;

        this.manuallyAdded =
                manuallyAdded;
    }

    public long getId() {
        return id;
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public String getSourceRawText() {
        return sourceRawText;
    }

    public boolean isManuallyAdded() {
        return manuallyAdded;
    }

    public ReferenceProposal withReference(
            WarehouseReference newReference
    ) {
        return new ReferenceProposal(
                id,
                newReference,
                sourceRawText,
                manuallyAdded
        );
    }
}