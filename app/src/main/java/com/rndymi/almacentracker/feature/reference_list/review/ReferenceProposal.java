package com.rndymi.almacentracker.feature.reference_list.review;

import com.rndymi.almacentracker.domain.reference.WarehouseReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReferenceProposal {

    private final long id;
    private final WarehouseReference reference;
    private final String sourceRawText;
    private final boolean manuallyAdded;
    private final boolean requiresCorrection;
    private final List<WarehouseReference> suggestions;

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded
    ) {
        this(
                id,
                reference,
                sourceRawText,
                manuallyAdded,
                false,
                Collections.emptyList()
        );
    }

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded,
            boolean requiresCorrection
    ) {
        this(
                id,
                reference,
                sourceRawText,
                manuallyAdded,
                requiresCorrection,
                Collections.emptyList()
        );
    }

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded,
            boolean requiresCorrection,
            List<WarehouseReference> suggestions
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

        this.requiresCorrection =
                requiresCorrection;

        this.suggestions =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                suggestions == null
                                        ? Collections.emptyList()
                                        : suggestions
                        )
                );
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

    public boolean requiresCorrection() {
        return requiresCorrection;
    }

    public List<WarehouseReference> getSuggestions() {
        return suggestions;
    }

    public ReferenceProposal withReference(
            WarehouseReference newReference
    ) {
        return new ReferenceProposal(
                id,
                newReference,
                sourceRawText,
                manuallyAdded,
                false,
                Collections.emptyList()
        );
    }
}
