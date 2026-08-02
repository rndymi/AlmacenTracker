package com.rndymi.almacentracker.feature.withdrawal_history.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WithdrawalHistoryRepository;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraft;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryDraftValidator;
import com.rndymi.almacentracker.domain.history.WithdrawalHistoryRecord;
import com.rndymi.almacentracker.domain.history.WithdrawalHistorySummary;
import com.rndymi.almacentracker.domain.history.WithdrawalLocationStatus;
import com.rndymi.almacentracker.feature.withdrawal_history.common.WithdrawalHistoryCreateInput;

import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public final class WithdrawalHistoryCreateViewModelTest {

    @Rule
    public final InstantTaskExecutorRule rule =
            new InstantTaskExecutorRule();

    @Test
    public void initialize_preservesDocumentProposal() {
        TestDependencies dependencies =
                new TestDependencies();

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

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
    public void requestSave_emitsConfirmation() {
        TestDependencies dependencies =
                new TestDependencies();

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.requestSaveConfirmation(
                1000L
        );

        UiEvent<WithdrawalHistoryDraft> event =
                viewModel.getConfirmationEvent()
                        .getValue();

        assertNotNull(event);

        WithdrawalHistoryDraft draft =
                event.getContentIfNotHandled();

        assertNotNull(draft);

        assertEquals(
                1,
                draft.getEntries().size()
        );

        assertEquals(
                "CAJAS",
                draft.getEntries()
                        .get(0)
                        .getUnit()
        );

        assertEquals(
                0,
                dependencies.repository.insertCalls
        );
    }

    @Test
    public void confirmSave_entersSavingState() {
        TestDependencies dependencies =
                new TestDependencies();

        dependencies.repository.automaticSuccess =
                false;

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.confirmSave(
                1000L
        );

        assertEquals(
                WithdrawalHistoryCreateUiState
                        .Status.SAVING,
                viewModel.getUiState()
                        .getValue()
                        .getStatus()
        );

        assertEquals(
                1,
                dependencies.repository.insertCalls
        );
    }

    @Test
    public void confirmSave_successEmitsGeneratedId() {
        TestDependencies dependencies =
                new TestDependencies();

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.confirmSave(
                1000L
        );

        assertEquals(
                WithdrawalHistoryCreateUiState
                        .Status.SAVED,
                viewModel.getUiState()
                        .getValue()
                        .getStatus()
        );

        UiEvent<Long> event =
                viewModel.getSavedEvent()
                        .getValue();

        assertNotNull(event);

        assertEquals(
                Long.valueOf(15L),
                event.getContentIfNotHandled()
        );
    }

    @Test
    public void confirmSave_ignoresDoubleTap() {
        TestDependencies dependencies =
                new TestDependencies();

        dependencies.repository.automaticSuccess =
                false;

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.confirmSave(
                1000L
        );

        viewModel.confirmSave(
                1000L
        );

        assertEquals(
                1,
                dependencies.repository.insertCalls
        );
    }

    @Test
    public void confirmSave_ignoresRepeatedCallback() {
        TestDependencies dependencies =
                new TestDependencies();

        dependencies.repository.automaticSuccess =
                false;

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.confirmSave(
                1000L
        );

        dependencies.repository.callback
                .onSuccess(15L);

        dependencies.repository.callback
                .onSuccess(16L);

        assertEquals(
                WithdrawalHistoryCreateUiState
                        .Status.SAVED,
                viewModel.getUiState()
                        .getValue()
                        .getStatus()
        );

        assertEquals(
                Long.valueOf(15L),
                viewModel.getSavedEvent()
                        .getValue()
                        .getContentIfNotHandled()
        );
    }

    @Test
    public void confirmSave_errorPreservesDraft() {
        TestDependencies dependencies =
                new TestDependencies();

        dependencies.repository.error =
                new RuntimeException(
                        "Room error"
                );

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.onTitleChanged(
                "Reposición centro"
        );

        viewModel.confirmSave(
                1000L
        );

        WithdrawalHistoryCreateUiState state =
                viewModel.getUiState()
                        .getValue();

        assertEquals(
                WithdrawalHistoryCreateUiState
                        .Status.ERROR,
                state.getStatus()
        );

        assertEquals(
                "Reposición centro",
                state.getTitle()
        );

        assertEquals(
                "4",
                state.getEntries()
                        .get(0)
                        .getQuantityText()
        );

        assertNotNull(
                state.getSaveError()
        );
    }

    @Test
    public void confirmSave_allowsRetryAfterError() {
        TestDependencies dependencies =
                new TestDependencies();

        dependencies.repository.error =
                new RuntimeException(
                        "First failure"
                );

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

        initialize(viewModel);

        viewModel.confirmSave(
                1000L
        );

        dependencies.repository.error = null;

        viewModel.confirmSave(
                1000L
        );

        assertEquals(
                2,
                dependencies.repository.insertCalls
        );

        assertEquals(
                WithdrawalHistoryCreateUiState
                        .Status.SAVED,
                viewModel.getUiState()
                        .getValue()
                        .getStatus()
        );
    }

    @Test
    public void requestSave_rejectsUnitWithoutQuantity() {
        TestDependencies dependencies =
                new TestDependencies();

        WithdrawalHistoryCreateViewModel viewModel =
                dependencies.createViewModel();

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

        viewModel.requestSaveConfirmation(
                1000L
        );

        WithdrawalHistoryCreateUiState state =
                viewModel.getUiState()
                        .getValue();

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

        assertNull(
                viewModel.getConfirmationEvent()
                        .getValue()
        );
    }

    private static void initialize(
            WithdrawalHistoryCreateViewModel viewModel
    ) {
        viewModel.initialize(
                Collections.singletonList(
                        foundInput(
                                4,
                                "CAJAS"
                        )
                ),
                1000L
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

    private static final class TestDependencies {

        private final FakeRepository repository =
                new FakeRepository();

        private WithdrawalHistoryCreateViewModel
        createViewModel() {
            WithdrawalHistorySaveService saveService =
                    new WithdrawalHistorySaveService(
                            repository,
                            () -> 2000L
                    );

            return new WithdrawalHistoryCreateViewModel(
                    saveService,
                    new WithdrawalHistoryDraftValidator()
            );
        }
    }

    private static final class FakeRepository
            implements WithdrawalHistoryRepository {

        private int insertCalls;
        private boolean automaticSuccess = true;
        private RuntimeException error;
        private RepositoryCallback<Long> callback;

        @Override
        public void insert(
                WithdrawalHistoryRecord record,
                RepositoryCallback<Long> callback
        ) {
            insertCalls++;
            this.callback = callback;

            if (error != null) {
                callback.onError(error);
                return;
            }

            if (automaticSuccess) {
                callback.onSuccess(15L);
            }
        }

        @Override
        public void findById(
                long historyId,
                RepositoryCallback<WithdrawalHistoryRecord> callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findAllSummaries(
                RepositoryCallback<
                        List<WithdrawalHistorySummary>> callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(
                long historyId,
                RepositoryCallback<Void> callback
        ) {
            throw new UnsupportedOperationException();
        }
    }
}
