package com.rndymi.almacentracker.feature.reference_list.review;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceMatch;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReferenceListReviewViewModel
        extends ViewModel {

    private final WarehouseReferenceParser parser;

    private final MutableLiveData
            <ReferenceListReviewUiState> uiState =
            new MutableLiveData<>(
                    ReferenceListReviewUiState
                            .initial()
            );

    private final MutableLiveData
            <UiEvent<List<WarehouseReference>>>
            confirmationEvent =
            new MutableLiveData<>();

    private final MutableLiveData
            <UiEvent<Integer>>
            duplicateConsolidatedEvent =
            new MutableLiveData<>();

    private long nextProposalId = 1L;

    public ReferenceListReviewViewModel(
            WarehouseReferenceParser parser
    ) {
        this.parser =
                Objects.requireNonNull(
                        parser,
                        "parser"
                );
    }

    public LiveData<ReferenceListReviewUiState>
    getUiState() {
        return uiState;
    }

    public LiveData
            <UiEvent<List<WarehouseReference>>>
    getConfirmationEvent() {
        return confirmationEvent;
    }

    public LiveData<UiEvent<Integer>>
    getDuplicateConsolidatedEvent() {
        return duplicateConsolidatedEvent;
    }

    public void applyInitialLines(
            List<String> lines
    ) {
        ReferenceListReviewUiState current =
                uiState.getValue();

        if (current != null
                && current.isInitialized()) {
            return;
        }

        Map<String, ReferenceProposal> unique =
                new LinkedHashMap<>();

        int duplicateCount = 0;

        if (lines != null) {
            for (
                    int lineIndex = 0;
                    lineIndex < lines.size();
                    lineIndex++
            ) {
                List<WarehouseReferenceMatch> matches =
                        parser.parseLine(
                                lineIndex,
                                lines.get(lineIndex)
                        );

                for (
                        WarehouseReferenceMatch match
                        : matches
                ) {
                    WarehouseReference reference =
                            match.getReference();

                    if (unique.containsKey(
                            reference.identityKey()
                    )) {
                        duplicateCount++;
                        continue;
                    }

                    unique.put(
                            reference.identityKey(),
                            new ReferenceProposal(
                                    nextProposalId++,
                                    reference,
                                    match.getSourceRawText(),
                                    false
                            )
                    );
                }
            }
        }

        uiState.setValue(
                new ReferenceListReviewUiState(
                        new ArrayList<>(
                                unique.values()
                        ),
                        true,
                        false
                )
        );

        if (duplicateCount > 0) {
            duplicateConsolidatedEvent
                    .setValue(
                            new UiEvent<>(
                                    duplicateCount
                            )
                    );
        }
    }

    public ReferenceInputResult addReference(
            String category,
            String code
    ) {
        WarehouseReference reference =
                parser.parseInput(
                        category,
                        code
                );

        if (reference == null) {
            return invalidResult(
                    category,
                    code
            );
        }

        List<ReferenceProposal> proposals =
                mutableProposals();

        if (containsReference(
                proposals,
                reference,
                -1L
        )) {
            return ReferenceInputResult
                    .duplicate();
        }

        proposals.add(
                new ReferenceProposal(
                        nextProposalId++,
                        reference,
                        null,
                        true
                )
        );

        publish(proposals);

        return ReferenceInputResult.success();
    }

    public ReferenceInputResult editReference(
            long proposalId,
            String category,
            String code
    ) {
        WarehouseReference reference =
                parser.parseInput(
                        category,
                        code
                );

        if (reference == null) {
            return invalidResult(
                    category,
                    code
            );
        }

        List<ReferenceProposal> proposals =
                mutableProposals();

        int proposalIndex =
                findIndex(
                        proposals,
                        proposalId
                );

        if (proposalIndex < 0) {
            return ReferenceInputResult
                    .notFound();
        }

        if (containsReference(
                proposals,
                reference,
                proposalId
        )) {
            return ReferenceInputResult
                    .duplicate();
        }

        ReferenceProposal current =
                proposals.get(
                        proposalIndex
                );

        proposals.set(
                proposalIndex,
                current.withReference(
                        reference
                )
        );

        publish(proposals);

        return ReferenceInputResult.success();
    }

    public void deleteReference(
            long proposalId
    ) {
        List<ReferenceProposal> proposals =
                mutableProposals();

        int proposalIndex =
                findIndex(
                        proposals,
                        proposalId
                );

        if (proposalIndex < 0) {
            return;
        }

        proposals.remove(
                proposalIndex
        );

        publish(proposals);
    }

    public void confirm() {
        ReferenceListReviewUiState current =
                uiState.getValue();

        if (current == null
                || !current.canConfirm()) {
            return;
        }

        List<WarehouseReference> references =
                new ArrayList<>();

        for (
                ReferenceProposal proposal
                : current.getProposals()
        ) {
            references.add(
                    proposal.getReference()
            );
        }

        uiState.setValue(
                new ReferenceListReviewUiState(
                        current.getProposals(),
                        true,
                        true
                )
        );

        confirmationEvent.setValue(
                new UiEvent<>(
                        references
                )
        );
    }

    private ReferenceInputResult invalidResult(
            String category,
            String code
    ) {
        String normalizedCategory =
                parser.normalizeCategory(
                        category
                );

        String normalizedCode =
                parser.normalizeCode(
                        code
                );

        return ReferenceInputResult.invalid(
                parser.isValidCategory(
                        normalizedCategory
                ),
                parser.isValidCode(
                        normalizedCode
                )
        );
    }

    private List<ReferenceProposal>
    mutableProposals() {
        ReferenceListReviewUiState current =
                uiState.getValue();

        if (current == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
                current.getProposals()
        );
    }

    private void publish(
            List<ReferenceProposal> proposals
    ) {
        uiState.setValue(
                new ReferenceListReviewUiState(
                        proposals,
                        true,
                        false
                )
        );
    }

    private boolean containsReference(
            List<ReferenceProposal> proposals,
            WarehouseReference reference,
            long excludedProposalId
    ) {
        for (
                ReferenceProposal proposal
                : proposals
        ) {
            if (proposal.getId()
                    == excludedProposalId) {
                continue;
            }

            if (proposal
                    .getReference()
                    .equals(reference)) {
                return true;
            }
        }

        return false;
    }

    private int findIndex(
            List<ReferenceProposal> proposals,
            long proposalId
    ) {
        for (
                int index = 0;
                index < proposals.size();
                index++
        ) {
            if (proposals
                    .get(index)
                    .getId()
                    == proposalId) {
                return index;
            }
        }

        return -1;
    }
}