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
import com.rndymi.almacentracker.domain.reference.DocumentReferenceData;
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
    public void applyInitialLinesOffersSuggestionAndResolvesInvalidCategory() {
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

        ReferenceProposal first =
                state.getProposals().get(0);

        assertEquals(
                "MR 2I57I",
                first.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus
                        .UNIQUE_SUGGESTION,
                first.getMatchStatus()
        );

        assertEquals(
                "MR 21571",
                first.getSuggestions()
                        .get(0)
                        .displayValue()
        );

        ReferenceProposal second =
                state.getProposals().get(1);

        assertEquals(
                "MS 5008",
                second.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus.EXACT,
                second.getMatchStatus()
        );

        assertFalse(second.requiresCorrection());
    }

    @Test
    public void applyInitialLinesJoinsSplitInputAndResolvesUniqueReference() {
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
                                        Collections.singletonList(
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

        ReferenceProposal proposal =
                state.getProposals().get(0);

        assertEquals(
                "MS 5008",
                proposal.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus.EXACT,
                proposal.getMatchStatus()
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
        assertEquals(
                ReferenceProposal.MatchStatus.AMBIGUOUS,
                proposal.getMatchStatus()
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
        assertEquals(
                ReferenceProposal.MatchStatus
                        .USER_CONFIRMED,
                resolved.getMatchStatus()
        );
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
        assertEquals(
                ReferenceProposal.MatchStatus.EXACT,
                proposal.getMatchStatus()
        );
    }

    @Test
    public void applyInitialLinesMarksUnknownReferenceAsNoMatch() {
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
                                        Collections.singletonList(
                                                warehouseItem(
                                                        "MR",
                                                        "1210"
                                                )
                                        )
                                );
                            }
                        }
                );

        resolvingViewModel.applyInitialLines(
                Collections.singletonList(
                        "MZ 9999"
                )
        );

        ReferenceProposal proposal =
                resolvingViewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                ReferenceProposal.MatchStatus.NO_MATCH,
                proposal.getMatchStatus()
        );
        assertTrue(proposal.requiresCorrection());
        assertTrue(proposal.getSuggestions().isEmpty());
        assertFalse(
                resolvingViewModel
                        .getUiState()
                        .getValue()
                        .canConfirm()
        );
    }

    @Test
    public void applyInitialLinesKeepsOnlyAlphabeticOcrCategoriesWithoutRepository() {
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
        assertEquals(2, state.getReferenceCount());

        assertEquals(
                "ME 21570",
                state.getProposals()
                        .get(0)
                        .getReference()
                        .displayValue()
        );

        assertEquals(
                "ML 3923",
                state.getProposals()
                        .get(1)
                        .getReference()
                        .displayValue()
        );

        assertFalse(state.canConfirm());
    }

    @Test
    public void applyInitialLinesSeparatesOcrCodeAndOptionalSuffix() {
        viewModel.applyInitialLines(
                Collections.singletonList(
                        "MR 215L1POS"
                )
        );

        ReferenceProposal proposal =
                viewModel.getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                "MR 215L1 POS",
                proposal.getReference()
                        .displayValue()
        );

        assertTrue(proposal.requiresCorrection());
    }

    @Test
    public void applyInitialLinesPreservesAttachedQualifierWithoutQuantity() {
        viewModel.applyInitialLines(
                Collections.singletonList(
                        "MR 21511pcs"
                )
        );

        ReferenceProposal proposal =
                viewModel.getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        assertEquals(
                "MR 21511 PCS",
                proposal.getReference()
                        .displayValue()
        );

        assertTrue(proposal.requiresCorrection());
    }

    @Test
    public void editReferenceClearsRequiredCorrectionState() {
        viewModel.applyInitialLines(
                Collections.singletonList(
                        "MR 215I1"
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

        assertEquals(
                null,
                added.getDocumentData().getQuantity()
        );
        assertEquals(
                null,
                added.getDocumentData().getUnit()
        );
        assertEquals(
                1,
                added.getDocumentData()
                        .getSourceLineIndex()
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
    public void editReference_preservesDocumentQuantityAndUnit() {
        viewModel.applyInitialLines(
                Collections.singletonList(
                        "MR 1210 - 20 PCS"
                )
        );

        ReferenceProposal proposal =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0);

        viewModel.editReference(
                proposal.getId(),
                "AB",
                "2026A"
        );

        DocumentReferenceData documentData =
                viewModel
                        .getUiState()
                        .getValue()
                        .getProposals()
                        .get(0)
                        .getDocumentData();

        assertEquals(
                "AB",
                documentData.getReference()
                        .getCategory()
        );
        assertEquals(
                Integer.valueOf(20),
                documentData.getQuantity()
        );
        assertEquals(
                "PCS",
                documentData.getUnit()
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
    public void confirm_emitsOrderedDocumentDataOnlyOnce() {
        viewModel.applyInitialLines(
                Arrays.asList(
                        "MR 1210 - 20 PCS",
                        "MZ1300C"
                )
        );

        viewModel.confirm();

        UiEvent<List<DocumentReferenceData>> event =
                viewModel
                        .getConfirmationEvent()
                        .getValue();

        assertNotNull(event);

        List<DocumentReferenceData> confirmedValues =
                event.getContentIfNotHandled();

        assertNotNull(confirmedValues);
        assertEquals(
                2,
                confirmedValues.size()
        );

        assertEquals(
                "MR",
                confirmedValues.get(0)
                        .getReference()
                        .getCategory()
        );

        assertEquals(
                Integer.valueOf(20),
                confirmedValues.get(0)
                        .getQuantity()
        );

        assertEquals(
                "PCS",
                confirmedValues.get(0)
                        .getUnit()
        );

        assertEquals(
                "MZ",
                confirmedValues.get(1)
                        .getReference()
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

    @Test
    public void applyInitialLinesProcessesRealPaddleOutput() {
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
                                                        "21570"
                                                ),
                                                warehouseItem(
                                                        "MR",
                                                        "21571"
                                                ),
                                                warehouseItem(
                                                        "MS",
                                                        "5008"
                                                ),
                                                warehouseItem(
                                                        "ML",
                                                        "3723"
                                                )
                                        )
                                );
                            }
                        }
                );

        resolvingViewModel.applyInitialLines(
                Arrays.asList(
                        "Clena",
                        "M221570-5pc5",
                        "Mn21571-1pcs",
                        "M55008-3pcs",
                        "ML3923-4p9fs"
                )
        );

        ReferenceListReviewUiState state =
                resolvingViewModel
                        .getUiState()
                        .getValue();

        assertNotNull(state);
        assertEquals(4, state.getReferenceCount());

        ReferenceProposal first =
                state.getProposals().get(0);

        assertEquals(
                "MR 21570",
                first.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus.EXACT,
                first.getMatchStatus()
        );

        assertEquals(
                Integer.valueOf(5),
                first.getDocumentData()
                        .getQuantity()
        );

        assertEquals(
                "PCS",
                first.getDocumentData()
                        .getUnit()
        );

        ReferenceProposal second =
                state.getProposals().get(1);

        assertEquals(
                "MN 21571",
                second.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus
                        .UNIQUE_SUGGESTION,
                second.getMatchStatus()
        );

        assertEquals(
                "MR 21571",
                second.getSuggestions()
                        .get(0)
                        .displayValue()
        );

        assertEquals(
                Integer.valueOf(1),
                second.getDocumentData()
                        .getQuantity()
        );

        assertEquals(
                "PCS",
                second.getDocumentData()
                        .getUnit()
        );

        ReferenceProposal third =
                state.getProposals().get(2);

        assertEquals(
                "MS 5008",
                third.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus.EXACT,
                third.getMatchStatus()
        );

        assertEquals(
                Integer.valueOf(3),
                third.getDocumentData()
                        .getQuantity()
        );

        assertEquals(
                "PCS",
                third.getDocumentData()
                        .getUnit()
        );

        ReferenceProposal fourth =
                state.getProposals().get(3);

        assertEquals(
                "ML 3923",
                fourth.getReference()
                        .displayValue()
        );

        assertEquals(
                ReferenceProposal.MatchStatus
                        .UNIQUE_SUGGESTION,
                fourth.getMatchStatus()
        );

        assertEquals(
                "ML 3723",
                fourth.getSuggestions()
                        .get(0)
                        .displayValue()
        );

        assertEquals(
                Integer.valueOf(4),
                fourth.getDocumentData()
                        .getQuantity()
        );

        assertEquals(
                "PQTS",
                fourth.getDocumentData()
                        .getUnit()
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
