package com.rndymi.almacentracker.feature.withdrawal_history.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateInput;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;

public final class WithdrawalHistoryCreateViewModelTest {

    @Rule
    public final InstantTaskExecutorRule rule =
            new InstantTaskExecutorRule();

    @Test
    public void initialize_preservesDocumentProposal() {
        WithdrawalHistoryCreateViewModel viewModel =
                createViewModel();

        viewModel.initialize(
                Collections.singletonList(
                        foundInput(
                                4,
                                "CAJAS"
                        )
                ),
                1000L
        );

        WithdrawalHistoryCreateUiState state =
                viewModel.getUiState().getValue();

        assertNotNull(state);
        assertEquals(
                "4",
                state.getEntries()
                        .get(0)
                        .getQuantityText()
        );
        assertEquals(
                "CAJAS",
                state.getEntries()
                        .get(0)
                        .getUnitText()
        );
    }

    @Test
    public void continue_allowsEmptyQuantityAndUnit() {
        WithdrawalHistoryCreateViewModel viewModel =
                createViewModel();

        viewModel.initialize(
                Collections.singletonList(
                        foundInput(
                                null,
                                null
                        )
                ),
                1000L
        );

        viewModel.continueToConfirmation(
                1000L
        );

        UiEvent<WithdrawalHistoryDraft> event =
                viewModel.getContinueEvent()
                        .getValue();

        assertNotNull(event);

        WithdrawalHistoryDraft draft =
                event.getContentIfNotHandled();

        assertNotNull(draft);
        assertNull(
                draft.getEntries()
                        .get(0)
                        .getQuantity()
        );
        assertNull(
                draft.getEntries()
                        .get(0)
                        .getUnit()
        );
    }

    @Test
    public void continue_rejectsUnitWithoutQuantity() {
        WithdrawalHistoryCreateViewModel viewModel =
                createViewModel();

        viewModel.initialize(
                Collections.singletonList(
                        foundInput(
                                null,
                                null
                        )
                ),
                1000L
        );

        long stableId =
                viewModel.getUiState()
                        .getValue()
                        .getEntries()
                        .get(0)
                        .getStableId();

        viewModel.onUnitChanged(
                stableId,
                "cajas"
        );

        viewModel.continueToConfirmation(
                1000L
        );

        WithdrawalHistoryCreateUiState state =
                viewModel.getUiState().getValue();

        assertEquals(
                WithdrawalHistoryCreateUiState
                        .Status.INVALID_INPUT,
                state.getStatus()
        );

        assertNotNull(
                state.getEntries()
                        .get(0)
                        .getUnitError()
        );
    }

    @Test
    public void continue_normalizesTitleAndUnit() {
        WithdrawalHistoryCreateViewModel viewModel =
                createViewModel();

        viewModel.initialize(
                Collections.singletonList(
                        foundInput(
                                4,
                                " cajas "
                        )
                ),
                1000L
        );

        viewModel.onTitleChanged(
                "  Reposición centro  "
        );

        viewModel.continueToConfirmation(
                1000L
        );

        WithdrawalHistoryDraft draft =
                viewModel.getContinueEvent()
                        .getValue()
                        .getContentIfNotHandled();

        assertEquals(
                "Reposición centro",
                draft.getTitle()
        );

        assertEquals(
                "CAJAS",
                draft.getEntries()
                        .get(0)
                        .getUnit()
        );
    }

    private static WithdrawalHistoryCreateViewModel
    createViewModel() {
        return new WithdrawalHistoryCreateViewModel(
                new WithdrawalHistoryDraftValidator()
        );
    }

    private static WithdrawalHistoryCreateInput
    foundInput(
            Integer quantity,
            String unit
    ) {
        return new WithdrawalHistoryCreateInput(
                0,
                "MR",
                "1210",
                quantity,
                unit,
                7L,
                "A1",
                "2",
                WithdrawalLocationStatus.FOUND
        );
    }
}
