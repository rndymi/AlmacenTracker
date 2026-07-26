package com.rndymi.almacentracker.feature.reference_list.review;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ReferenceListReviewUiState {

    private final List<ReferenceProposal> proposals;
    private final boolean initialized;
    private final boolean confirming;

    public ReferenceListReviewUiState(
            List<ReferenceProposal> proposals,
            boolean initialized,
            boolean confirming
    ) {
        this.proposals =
                Collections.unmodifiableList(
                        new ArrayList<>(
                                proposals
                        )
                );

        this.initialized =
                initialized;

        this.confirming =
                confirming;
    }

    public static ReferenceListReviewUiState initial() {
        return new ReferenceListReviewUiState(
                Collections.emptyList(),
                false,
                false
        );
    }

    public List<ReferenceProposal> getProposals() {
        return proposals;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isConfirming() {
        return confirming;
    }

    public boolean isEmpty() {
        return proposals.isEmpty();
    }

    public boolean canConfirm() {
        return initialized
                && !confirming
                && !proposals.isEmpty();
    }

    public int getReferenceCount() {
        return proposals.size();
    }
}