package com.rndymi.almacentracker.adapter.in.ui.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.adapter.in.ui.state.DataManagementUiState;
import com.rndymi.almacentracker.adapter.in.ui.state.UiEvent;
import com.rndymi.almacentracker.application.port.in.RestoreWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.port.in.ValidateWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.result.RestoreWarehouseBackupResult;
import com.rndymi.almacentracker.application.result.WarehouseBackupValidationResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.LiveDataTestUtil;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public final class DataManagementViewModelTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    private FakeValidateBackupUseCase validateUseCase;
    private FakeRestoreBackupUseCase restoreUseCase;
    private DataManagementViewModel viewModel;

    @Before
    public void setUp() {
        validateUseCase = new FakeValidateBackupUseCase();
        restoreUseCase = new FakeRestoreBackupUseCase();

        viewModel = new DataManagementViewModel(
                (destination, callback) -> { },
                callback -> { },
                (source, callback) -> { },
                (destination, callback) -> { },
                validateUseCase,
                restoreUseCase,
                () -> "warehouse.csv",
                () -> "warehouse-backup.csv"
        );
    }

    @Test
    public void requestBackupRestoreSource_requestsDocument()
            throws InterruptedException {
        viewModel.requestBackupRestoreSource();

        DataManagementUiState state = state();
        UiEvent<Boolean> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getBackupSourceRequest()
                );

        assertEquals(
                DataManagementUiState.Status.SELECTING_BACKUP_SOURCE,
                state.getStatus()
        );
        assertEquals(Boolean.TRUE, event.getContentIfNotHandled());
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void selectBackupSource_startsValidation()
            throws InterruptedException {
        viewModel.requestBackupRestoreSource();
        viewModel.onBackupRestoreSourceSelected(
                "content://backup"
        );

        assertEquals(
                DataManagementUiState.Status.VALIDATING_BACKUP,
                state().getStatus()
        );
        assertEquals(1, validateUseCase.calls);
        assertEquals(
                "content://backup",
                validateUseCase.sourceReference
        );
    }

    @Test
    public void cancelBackupSourceSelection_returnsToIdle()
            throws InterruptedException {
        viewModel.requestBackupRestoreSource();
        viewModel.onBackupRestoreSourceSelected(null);

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(0, validateUseCase.calls);
    }

    @Test
    public void validBackup_requestsConfirmationWithItemCount()
            throws InterruptedException {
        WarehouseItem item = item();

        startValidation();
        validateUseCase.callback.onResult(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item)
                )
        );

        DataManagementUiState state = state();
        UiEvent<Integer> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getBackupRestoreConfirmation()
                );

        assertEquals(
                DataManagementUiState.Status.BACKUP_READY,
                state.getStatus()
        );
        assertEquals(1, state.getPendingRestoreCount());
        assertEquals(Integer.valueOf(1), event.peekContent());
    }

    @Test
    public void confirmBackupRestore_startsRestoreWithSnapshot()
            throws InterruptedException {
        WarehouseItem item = item();

        startValidation();
        validateUseCase.callback.onResult(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item)
                )
        );

        viewModel.confirmBackupRestore();

        assertEquals(
                DataManagementUiState.Status.RESTORING_BACKUP,
                state().getStatus()
        );
        assertEquals(1, restoreUseCase.calls);
        assertEquals(1, restoreUseCase.items.size());
        assertEquals(item, restoreUseCase.items.get(0));
        assertNotSame(
                validateUseCase.resultItems,
                restoreUseCase.items
        );
    }

    @Test
    public void cancelBackupRestore_discardsConfirmation()
            throws InterruptedException {
        startValidation();
        validateUseCase.callback.onResult(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item())
                )
        );

        viewModel.cancelBackupRestore();

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(0, restoreUseCase.calls);
    }

    @Test
    public void invalidBackup_exposesErrorWithRowNumber()
            throws InterruptedException {
        startValidation();
        validateUseCase.callback.onResult(
                WarehouseBackupValidationResult.failure(
                        WarehouseBackupValidationResult.Status.INVALID_DATA,
                        7,
                        "Invalid date",
                        null
                )
        );

        DataManagementUiState state = state();

        assertEquals(
                DataManagementUiState.Status.ERROR,
                state.getStatus()
        );
        assertTrue(state.getMessage().contains("Fila 7."));
    }

    @Test
    public void successfulRestore_returnsToIdleAndEmitsCount()
            throws InterruptedException {
        startValidation();
        validateUseCase.callback.onResult(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item())
                )
        );
        viewModel.confirmBackupRestore();

        restoreUseCase.callback.onResult(
                RestoreWarehouseBackupResult.success(1)
        );

        UiEvent<Integer> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getBackupRestoreSuccess()
                );

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(Integer.valueOf(1), event.getContentIfNotHandled());
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void duplicateRestore_exposesError()
            throws InterruptedException {
        startValidation();
        validateUseCase.callback.onResult(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item())
                )
        );
        viewModel.confirmBackupRestore();

        restoreUseCase.callback.onResult(
                RestoreWarehouseBackupResult.failure(
                        RestoreWarehouseBackupResult.Status.DUPLICATE_DATA,
                        new IllegalStateException("Duplicate")
                )
        );

        DataManagementUiState state = state();

        assertEquals(
                DataManagementUiState.Status.ERROR,
                state.getStatus()
        );
        assertEquals(
                "La copia contiene mercancía duplicada.",
                state.getMessage()
        );
    }

    private void startValidation() {
        viewModel.requestBackupRestoreSource();
        viewModel.onBackupRestoreSourceSelected(
                "content://backup"
        );
    }

    private DataManagementUiState state()
            throws InterruptedException {
        return LiveDataTestUtil.getOrAwaitValue(
                viewModel.getUiState()
        );
    }

    private WarehouseItem item() {
        return new WarehouseItem(
                0L,
                "MR",
                "1050",
                "A1",
                null,
                null,
                1000L,
                2000L
        );
    }

    private static final class FakeValidateBackupUseCase
            implements ValidateWarehouseBackupUseCase {

        private int calls;
        private String sourceReference;
        private Callback callback;
        private List<WarehouseItem> resultItems;

        @Override
        public void validateBackup(
                String sourceReference,
                Callback callback
        ) {
            calls++;
            this.sourceReference = sourceReference;
            this.callback = result -> {
                resultItems = result.getWarehouseItems();
                callback.onResult(result);
            };
        }
    }

    private static final class FakeRestoreBackupUseCase
            implements RestoreWarehouseBackupUseCase {

        private int calls;
        private List<WarehouseItem> items;
        private Callback callback;

        @Override
        public void restoreBackup(
                List<WarehouseItem> warehouseItems,
                Callback callback
        ) {
            calls++;
            items = warehouseItems;
            this.callback = callback;
        }
    }
}
