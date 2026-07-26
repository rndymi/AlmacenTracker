package com.rndymi.almacentracker.feature.reference_list.review;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing
        .InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.domain.reference.WarehouseReference;
import com.rndymi.almacentracker.domain.reference.WarehouseReferenceParser;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
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
                        " 001300 c "
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
                "001300C",
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
}