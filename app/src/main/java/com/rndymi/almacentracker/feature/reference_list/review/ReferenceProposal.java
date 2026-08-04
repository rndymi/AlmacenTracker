package com.rndymi.almacentracker.feature.reference_list.review;

import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceSuggestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class ReferenceProposal {

    public enum MatchStatus {
        EXACT,
        UNIQUE_SUGGESTION,
        AMBIGUOUS,
        NO_MATCH,
        UNVERIFIED,
        USER_CONFIRMED
    }

    private final long id;
    private final WarehouseReference reference;
    private final WarehouseReference observedReference;
    private final String sourceRawText;
    private final boolean manuallyAdded;
    private final MatchStatus matchStatus;
    private final List<WarehouseReference> suggestions;
    private final List<WarehouseReferenceSuggestion> contextualSuggestions;
    private final DocumentReferenceData documentData;

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded
    ) {
        this(
                id,
                reference,
                reference,
                sourceRawText,
                manuallyAdded,
                MatchStatus.USER_CONFIRMED,
                Collections.emptyList(),
                Collections.emptyList(),
                null
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
                reference,
                sourceRawText,
                manuallyAdded,
                requiresCorrection
                        ? MatchStatus.NO_MATCH
                        : MatchStatus.EXACT,
                Collections.emptyList(),
                Collections.emptyList(),
                null
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
        this(
                id,
                reference,
                reference,
                sourceRawText,
                manuallyAdded,
                requiresCorrection
                        ? statusForSuggestions(suggestions)
                        : MatchStatus.EXACT,
                suggestions,
                Collections.emptyList(),
                null
        );
    }

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded,
            MatchStatus matchStatus,
            List<WarehouseReference> suggestions
    ) {
        this(
                id,
                reference,
                reference,
                sourceRawText,
                manuallyAdded,
                matchStatus,
                suggestions,
                Collections.emptyList(),
                null
        );
    }

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            String sourceRawText,
            boolean manuallyAdded,
            MatchStatus matchStatus,
            List<WarehouseReference> suggestions,
            DocumentReferenceData documentData
    ) {
        this(
                id,
                reference,
                reference,
                sourceRawText,
                manuallyAdded,
                matchStatus,
                suggestions,
                Collections.emptyList(),
                documentData
        );
    }

    public ReferenceProposal(
            long id,
            WarehouseReference reference,
            WarehouseReference observedReference,
            String sourceRawText,
            boolean manuallyAdded,
            MatchStatus matchStatus,
            List<WarehouseReference> suggestions,
            List<WarehouseReferenceSuggestion> contextualSuggestions,
            DocumentReferenceData documentData
    ) {
        this.id = id;

        this.reference =
                Objects.requireNonNull(
                        reference,
                        "reference"
                );

        this.observedReference =
                observedReference == null
                        ? reference
                        : observedReference;

        this.sourceRawText =
                sourceRawText;

        this.manuallyAdded =
                manuallyAdded;

        this.matchStatus =
                Objects.requireNonNull(
                        matchStatus,
                        "matchStatus"
                );

        this.suggestions =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                suggestions == null
                                        ? Collections.emptyList()
                                        : suggestions
                        )
                );

        this.contextualSuggestions =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                contextualSuggestions == null
                                        ? Collections.emptyList()
                                        : contextualSuggestions
                        )
                );

        this.documentData =
                documentData == null
                        ? new DocumentReferenceData(
                                reference,
                                null,
                                null,
                                0,
                                sourceRawText
                        )
                        : documentData;
    }

    public long getId() {
        return id;
    }

    public WarehouseReference getReference() {
        return reference;
    }

    public WarehouseReference getObservedReference() {
        return observedReference;
    }

    public String getSourceRawText() {
        return sourceRawText;
    }

    public boolean isManuallyAdded() {
        return manuallyAdded;
    }

    public boolean requiresCorrection() {
        return documentData.hasQuantityAmbiguity()
                || matchStatus
                == MatchStatus.UNIQUE_SUGGESTION
                || matchStatus
                == MatchStatus.AMBIGUOUS
                || matchStatus
                == MatchStatus.NO_MATCH;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public List<WarehouseReference> getSuggestions() {
        return suggestions;
    }

    public List<WarehouseReferenceSuggestion>
    getContextualSuggestions() {
        return contextualSuggestions;
    }

    public DocumentReferenceData getDocumentData() {
        return documentData;
    }

    public ReferenceProposal withReference(
            WarehouseReference newReference
    ) {
        DocumentReferenceData updatedDocumentData =
                new DocumentReferenceData(
                        newReference,
                        documentData.getObservedReference(),
                        documentData.getQuantity(),
                        documentData.getUnit(),
                        documentData.getSourceLineIndex(),
                        documentData.getSourceText(),
                        documentData.getQuantitySuggestions(),
                        documentData.getDestinations()
                );

        return new ReferenceProposal(
                id,
                newReference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                MatchStatus.USER_CONFIRMED,
                Collections.emptyList(),
                contextualSuggestions,
                updatedDocumentData
        );
    }

    public ReferenceProposal withQuantity(
            int quantity
    ) {
        DocumentReferenceData updatedDocumentData =
                new DocumentReferenceData(
                        reference,
                        documentData.getObservedReference(),
                        quantity,
                        documentData.getUnit(),
                        documentData.getSourceLineIndex(),
                        documentData.getSourceText(),
                        Collections.emptyList(),
                        documentData.getDestinations()
                );

        return new ReferenceProposal(
                id,
                reference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                matchStatus,
                suggestions,
                contextualSuggestions,
                updatedDocumentData
        );
    }

    private static MatchStatus statusForSuggestions(
            List<WarehouseReference> suggestions
    ) {
        int suggestionCount =
                suggestions == null
                        ? 0
                        : suggestions.size();

        if (suggestionCount == 1) {
            return MatchStatus.UNIQUE_SUGGESTION;
        }

        if (suggestionCount > 1) {
            return MatchStatus.AMBIGUOUS;
        }

        return MatchStatus.NO_MATCH;
    }
}
