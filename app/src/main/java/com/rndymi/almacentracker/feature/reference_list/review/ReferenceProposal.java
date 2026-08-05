package com.rndymi.almacentracker.feature.reference_list.review;

import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
import com.rndymi.almacentracker.domain.reference.DocumentReferenceAllocation;
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

    public enum ReviewState {
        AUTOMATIC,
        APPROVED,
        NEEDS_REVIEW
    }

    private final long id;
    private final WarehouseReference reference;
    private final WarehouseReference observedReference;
    private final String sourceRawText;
    private final boolean manuallyAdded;
    private final MatchStatus matchStatus;
    private final ReviewState reviewState;
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
                ReviewState.AUTOMATIC,
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
                ReviewState.AUTOMATIC,
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
                ReviewState.AUTOMATIC,
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
        this(
                id,
                reference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                matchStatus,
                ReviewState.AUTOMATIC,
                suggestions,
                contextualSuggestions,
                documentData
        );
    }

    private ReferenceProposal(
            long id,
            WarehouseReference reference,
            WarehouseReference observedReference,
            String sourceRawText,
            boolean manuallyAdded,
            MatchStatus matchStatus,
            ReviewState reviewState,
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

        this.reviewState =
                Objects.requireNonNull(
                        reviewState,
                        "reviewState"
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
                || !isCodeAccepted();
    }

    public boolean isCodeAccepted() {
        if (reviewState == ReviewState.APPROVED) {
            return true;
        }

        if (reviewState == ReviewState.NEEDS_REVIEW) {
            return false;
        }

        return matchStatus == MatchStatus.EXACT
                || matchStatus == MatchStatus.USER_CONFIRMED
                || matchStatus == MatchStatus.UNVERIFIED;
    }

    public MatchStatus getMatchStatus() {
        return matchStatus;
    }

    public ReviewState getReviewState() {
        return reviewState;
    }

    public boolean shouldShowSuggestions() {
        return !isCodeAccepted();
    }

    public ReferenceProposal toggleReviewState() {
        return copy(
                reference,
                matchStatus,
                isCodeAccepted()
                        ? ReviewState.NEEDS_REVIEW
                        : ReviewState.APPROVED,
                suggestions,
                contextualSuggestions,
                documentData
        );
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
                        documentData.getDestinations(),
                        documentData.getAllocations()
                );

        return new ReferenceProposal(
                id,
                newReference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                MatchStatus.USER_CONFIRMED,
                ReviewState.AUTOMATIC,
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
                        documentData.getDestinations(),
                        documentData.getAllocations()
                );

        return new ReferenceProposal(
                id,
                reference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                matchStatus,
                reviewState,
                suggestions,
                contextualSuggestions,
                updatedDocumentData
        );
    }

    private ReferenceProposal copy(
            WarehouseReference updatedReference,
            MatchStatus updatedMatchStatus,
            ReviewState updatedReviewState,
            List<WarehouseReference> updatedSuggestions,
            List<WarehouseReferenceSuggestion> updatedContextualSuggestions,
            DocumentReferenceData updatedDocumentData
    ) {
        return new ReferenceProposal(
                id,
                updatedReference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                updatedMatchStatus,
                updatedReviewState,
                updatedSuggestions,
                updatedContextualSuggestions,
                updatedDocumentData
        );
    }

    public ReferenceProposal withAllocation(
            DocumentReferenceAllocation allocation
    ) {
        List<DocumentReferenceAllocation> allocations =
                new ArrayList<>(
                        documentData.getAllocations()
                );

        if (allocation != null
                && !allocations.contains(allocation)) {
            allocations.add(allocation);
        }

        DocumentReferenceData updatedDocumentData =
                new DocumentReferenceData(
                        reference,
                        documentData.getObservedReference(),
                        documentData.getQuantity(),
                        documentData.getUnit(),
                        documentData.getSourceLineIndex(),
                        documentData.getSourceText(),
                        documentData.getQuantitySuggestions(),
                        documentData.getDestinations(),
                        allocations
                );

        return new ReferenceProposal(
                id,
                reference,
                observedReference,
                sourceRawText,
                manuallyAdded,
                matchStatus,
                reviewState,
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
