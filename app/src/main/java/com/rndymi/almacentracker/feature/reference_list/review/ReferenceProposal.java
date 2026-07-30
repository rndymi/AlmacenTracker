package com.rndymi.almacentracker.feature.reference_list.review;

import com.rndymi.almacentracker.domain.reference.WarehouseReference;

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
    private final String sourceRawText;
    private final boolean manuallyAdded;
    private final MatchStatus matchStatus;
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
                MatchStatus.USER_CONFIRMED,
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
                requiresCorrection
                        ? MatchStatus.NO_MATCH
                        : MatchStatus.EXACT,
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
        this(
                id,
                reference,
                sourceRawText,
                manuallyAdded,
                requiresCorrection
                        ? statusForSuggestions(suggestions)
                        : MatchStatus.EXACT,
                suggestions
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
        return matchStatus
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

    public ReferenceProposal withReference(
            WarehouseReference newReference
    ) {
        return new ReferenceProposal(
                id,
                newReference,
                sourceRawText,
                manuallyAdded,
                MatchStatus.USER_CONFIRMED,
                Collections.emptyList()
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
