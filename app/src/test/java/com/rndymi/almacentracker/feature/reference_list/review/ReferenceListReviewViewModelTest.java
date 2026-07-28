package com.rndymi.almacentracker.feature.reference_list.review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing
        .InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class ReferenceListReviewViewModelTest {

    @Rule
    public final InstantTaskExecutorRule
            instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    private ReferenceListReviewViewModel viewModel;

    @Before
    public void setUp() {
        viewModel =
                new ReferenceListReviewViewModel(
                        new WarehouseReferenceParser()
                );
    }

    @Test
    public void applyInitialLines_extractsAndPreservesOrder() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MZ1300C",
                        "MR1210A"
                )
        );

        ReferenceListReviewUiState state =
                viewModel
                        .getUiState()
                        .getValue();

        assertNotNull(state);
        assertEquals(
                2,
                state.getReferenceCount()
        );

        assertEquals(
                "MZ",
                state.getProposals()
                        .get(0)
                        .getReference()
                        .getCategory()
        );

        assertEquals(
                "MR",
                state.getProposals()
                        .get(1)
                        .getReference()
                        .getCategory()
        );
    }

    @Test
    public void applyInitialLines_consolidatesDuplicates() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210A",
                        "MR 1210 A",
                        "mr1210a"
                )
        );

        ReferenceListReviewUiState state =
                viewModel
                        .getUiState()
                        .getValue();

        assertEquals(
                1,
                state.getReferenceCount()
        );

        UiEvent<Integer> event =
                viewModel
                        .getDuplicateConsolidatedEvent()
                        .getValue();

        assertNotNull(event);

        assertEquals(
                Integer.valueOf(2),
                event.getContentIfNotHandled()
        );
    }

    @Test
    public void applyInitialLines_offersCandidatesWithoutAutoCorrecting() {
        ReferenceListReviewViewModel resolvingViewModel =
                new ReferenceListReviewViewModel(
                        new WarehouseReferenceParser(),
                        new WarehouseItemRepositoryStub() {

                            @Override
                            public void findAll(
                                    RepositoryCallback<
                                            List<WarehouseItem>
                                            > callback
                            ) {
                                callback.onSuccess(
                                        Arrays.asList(
                                                warehouseItem(
                                                        "MR",
                                                        "21571"
                                                ),
                                                warehouseItem(
                                                        "MS",
                                                        "5008"
                                                )
                                        )
                                );
                            }
                        }
                );

        resolvingViewModel.applyInitialLines(
                Arrays.asList(
                        "MR 2I57I - 1 pcs",
                        "M5 SOO8 - 3 pcs"
                )
        );

        ReferenceListReviewUiState state =
                resolvingViewModel
                        .getUiState()
                        .getValue();

        assertNotNull(state);
        assertEquals(2, state.getReferenceCount());
        assertEquals(
                "MR 2I57I",
                state.getProposals()
                        .get(0)
                        .getReference()
                        .displayValue()
        );
        assertEquals(
                "M5 SOO8",
                state.getProposals()
                        .get(1)
                        .getReference()
                        .displayValue()
        );
        assertTrue(
                state.getProposals()
                        .get(0)
                        .requiresCorrection()
        );
        assertEquals(
                "MR 21571",
                state.getProposals()
                        .get(0)
                        .getSuggestions()
                        .get(0)
                        .displayValue()
        );
        assertEquals(
                "MS 5008",
                state.getProposals()
                        .get(1)
                        .getSuggestions()
                        .get(0)
                        .displayValue()
        );
    }

    @Test
    public void applyInitialLines_joinsSplitInputAndOffersSuggestion() {
        ReferenceListReviewViewModel resolvingViewModel =
                new ReferenceListReviewViewModel(
                        new WarehouseReferenceParser(),
                        new WarehouseItemRepositoryStub() {

                            @Override
                            public void findAll(
                                    RepositoryCallback<
                                            List<WarehouseItem>
                                            > callback
                            ) {
                                callback.onSuccess(
                                        Arrays.asList(
                                                warehouseItem(
                                                        "MS",
                                                        "5008"
                                                )
                                        )
                                );
                            }
                        }
                );

        resolvingViewModel.applyInitialLines(
                Arrays.asList(
                        "M5",
                        "SOO8 - 3 pcs"
                )
        );

        ReferenceListReviewUiState state =
                resolvingViewModel
                        .getUiState()
                        .getValue();

        assertNotNull(state);
        assertEquals(1, state.getReferenceCount());
        assertEquals(
                "M5 SOO8",
                state.getProposals()
                        .get(0)
                        .getReference()
                        .displayValue()
        );
        assertEquals(
                "MS 5008",
                state.getProposals()
                        .get(0)
                        .getSuggestions()
                        .get(0)
                        .displayValue()
        );
    }

    @Test
    public void applySuggestionReplacesOnlySelectedProposal() {
        ReferenceListReviewViewModel resolvingViewModel =
                new ReferenceListReviewViewModel(
                        new WarehouseReferenceParser(),
                        new WarehouseItemRepositoryStub() {

                            @Override
                            public void findAll(
                                    RepositoryCallback<
                                            List<WarehouseItem>
                                            > callback
                            ) {
                                callback.onSuccess(
                                        Arrays.asList(
                                                warehouseItem(
                                                        "MR",
                                                        "21511"
                                                ),
                                                warehouseItem(
                                                        "MR",
                                                        "21571"
                                                )
                                        )
                                );
                            }
                        }
                );

        resolvingViewModel.applyInitialLines(
                Arrays.asList(
                        "MR 215I1"
                )
        );

        ReferenceProposal proposal =
                resolvingViewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                2,
                proposal.getSuggestions().size()
        );

        WarehouseReference selected =
                proposal.getSuggestions().get(1);

        ReferenceInputResult result =
                resolvingViewModel.applySuggestion(
                        proposal.getId(),
                        selected
                );

        assertEquals(
                ReferenceInputResult.Status.SUCCESS,
                result.getStatus()
        );

        ReferenceProposal resolved =
                resolvingViewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                "MR 21571",
                resolved.getReference().displayValue()
        );
        assertTrue(resolved.getSuggestions().isEmpty());
        assertFalse(resolved.requiresCorrection());
        assertTrue(
                resolvingViewModel
                        .getUiState()
                        .getValue()
                        .canConfirm()
        );
    }

    @Test
    public void applyInitialLinesDoesNotSuggestForExactKnownReference() {
        ReferenceListReviewViewModel resolvingViewModel =
                new ReferenceListReviewViewModel(
                        new WarehouseReferenceParser(),
                        new WarehouseItemRepositoryStub() {

                            @Override
                            public void findAll(
                                    RepositoryCallback<
                                            List<WarehouseItem>
                                            > callback
                            ) {
                                callback.onSuccess(
                                        Arrays.asList(
                                                warehouseItem(
                                                        "MD",
                                                        "1010 YELLOW"
                                                )
                                        )
                                );
                            }
                        }
                );

        resolvingViewModel.applyInitialLines(
                Arrays.asList(
                        "MD 1010 Yellow"
                )
        );

        ReferenceProposal proposal =
                resolvingViewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                "MD 1010 YELLOW",
                proposal.getReference().displayValue()
        );
        assertTrue(proposal.getSuggestions().isEmpty());
        assertFalse(proposal.requiresCorrection());
    }

    @Test
    public void applyInitialLines_keepsAllOcrCandidatesForReview() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "Me 21570 -5pcs",
                        "M2 215I1 pcs",
                        "M5 5008-3pcs",
                        "ML 3923-4gts"
                )
        );

        ReferenceListReviewUiState state =
                viewModel
                        .getUiState()
                        .getValue();

        assertNotNull(state);
        assertEquals(4, state.getReferenceCount());
        assertFalse(state.canConfirm());
        assertTrue(
                state.getProposals()
                        .get(1)
                        .requiresCorrection()
        );
        assertTrue(
                state.getProposals()
                        .get(2)
                        .requiresCorrection()
        );
        assertFalse(
                state.getProposals()
                        .get(3)
                        .requiresCorrection()
        );
    }

    @Test
    public void applyInitialLinesSeparatesOcrCodeAndOptionalSuffix() {
        viewModel.applyInitialLines(
                Collections.singletonList(
                        "M2 215L1POS"
                )
        );

        ReferenceProposal proposal =
                viewModel.getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                "M2 215L1 POS",
                proposal.getReference()
                        .displayValue()
        );
        assertTrue(proposal.requiresCorrection());
    }

    @Test
    public void applyInitialLinesDoesNotTreatAttachedUnitAsSuffix() {
        viewModel.applyInitialLines(
                Collections.singletonList(
                        "M2 21511pcs"
                )
        );

        ReferenceProposal proposal =
                viewModel.getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                "M2 21511",
                proposal.getReference()
                        .displayValue()
        );
        assertTrue(proposal.requiresCorrection());
    }

    @Test
    public void editReference_clearsRequiredCorrectionState() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "M2 215I1"
                )
        );

        ReferenceProposal proposal =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertTrue(proposal.requiresCorrection());

        ReferenceInputResult result =
                viewModel.editReference(
                        proposal.getId(),
                        "MR",
                        "21571"
                );

        assertEquals(
                ReferenceInputResult.Status.SUCCESS,
                result.getStatus()
        );
        assertFalse(
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0)
                        .requiresCorrection()
        );
        assertTrue(
                viewModel
                        .getUiState()
                        .getValue()
                        .canConfirm()
        );
    }

    @Test
    public void applyInitialLines_isOnlyAppliedOnce() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210"
                )
        );

        viewModel.addReference(
                "MZ",
                "1300C"
        );

        viewModel.applyInitialLines(
                Arrays.asList(
                        "AB2026"
                )
        );

        ReferenceListReviewUiState state =
                viewModel
                        .getUiState()
                        .getValue();

        assertEquals(
                2,
                state.getReferenceCount()
        );
    }

    @Test
    public void addReference_normalizesAndAppends() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210"
                )
        );

        ReferenceInputResult result =
                viewModel.addReference(
                        " mz ",
                        " 01300 c "
                );

        assertEquals(
                ReferenceInputResult.Status.SUCCESS,
                result.getStatus()
        );

        ReferenceProposal added =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(1);

        assertEquals(
                "MZ",
                added.getReference()
                        .getCategory()
        );

        assertEquals(
                "01300 C",
                added.getReference()
                        .getCode()
        );

        assertTrue(
                added.isManuallyAdded()
        );
    }

    @Test
    public void addReference_rejectsDuplicate() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210A"
                )
        );

        ReferenceInputResult result =
                viewModel.addReference(
                        "mr",
                        "1210a"
                );

        assertEquals(
                ReferenceInputResult.Status.DUPLICATE,
                result.getStatus()
        );

        assertEquals(
                1,
                viewModel
                        .getUiState()
                        .getValue()
                        .getReferenceCount()
        );
    }

    @Test
    public void editReference_preservesPosition() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210",
                        "MZ1300"
                )
        );

        ReferenceProposal first =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        ReferenceInputResult result =
                viewModel.editReference(
                        first.getId(),
                        "AB",
                        "2026A"
                );

        assertEquals(
                ReferenceInputResult.Status.SUCCESS,
                result.getStatus()
        );

        assertEquals(
                "AB",
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0)
                        .getReference()
                        .getCategory()
        );
    }

    @Test
    public void editReference_rejectsDuplicate() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210",
                        "MZ1300"
                )
        );

        ReferenceProposal second =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(1);

        ReferenceInputResult result =
                viewModel.editReference(
                        second.getId(),
                        "MR",
                        "1210"
                );

        assertEquals(
                ReferenceInputResult.Status.DUPLICATE,
                result.getStatus()
        );
    }

    @Test
    public void deleteReference_canLeaveEmptyState() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210"
                )
        );

        long proposalId =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0)
                        .getId();

        viewModel.deleteReference(
                proposalId
        );

        ReferenceListReviewUiState state =
                viewModel
                        .getUiState()
                        .getValue();

        assertTrue(
                state.isEmpty()
        );

        assertFalse(
                state.canConfirm()
        );
    }

    @Test
    public void confirm_emitsOrderedReferencesOnlyOnce() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR1210",
                        "MZ1300C"
                )
        );

        viewModel.confirm();

        UiEvent<List<WarehouseReference>> event =
                viewModel
                        .getConfirmationEvent()
                        .getValue();

        assertNotNull(event);

        List<WarehouseReference> references =
                event.getContentIfNotHandled();

        assertNotNull(references);
        assertEquals(
                2,
                references.size()
        );

        assertEquals(
                "MR",
                references.get(0)
                        .getCategory()
        );

        assertEquals(
                "MZ",
                references.get(1)
                        .getCategory()
        );

        assertEquals(
                null,
                event.getContentIfNotHandled()
        );
    }

    @Test
    public void confirm_doesNothingWhenListIsEmpty() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "Texto sin referencia"
                )
        );

        viewModel.confirm();

        assertEquals(
                null,
                viewModel
                        .getConfirmationEvent()
                        .getValue()
        );
    }

    private WarehouseItem warehouseItem(
            String category,
            String code
    ) {
        return new WarehouseItem(
                1L,
                category,
                code,
                "A1",
                null,
                null,
                1L,
                1L
        );
    }
}
