package com.rndymi.almacentracker.feature.data_management.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

import com.rndymi.almacentracker.core.common.event.UiEvent;
import com.rndymi.almacentracker.feature.data_management.backup.create.CreateWarehouseBackupResult;
import com.rndymi.almacentracker.feature.data_management.backup.create.CreateWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.RestoreWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.ValidateWarehouseBackupService;
import com.rndymi.almacentracker.feature.data_management.backup.restore.RestoreWarehouseBackupResult;
import com.rndymi.almacentracker.feature.data_management.export.ExportWarehouseItemsResult;
import com.rndymi.almacentracker.feature.data_management.export.ExportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.import_data.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.feature.data_management.import_data.ImportWarehouseItemsService;
import com.rndymi.almacentracker.feature.data_management.share.ShareWarehouseItemsResult;
import com.rndymi.almacentracker.feature.data_management.share.ShareWarehouseItemsService;
import com.rndymi.almacentracker.core.csv.share.ShareableCsvFile;
import com.rndymi.almacentracker.feature.data_management.backup.restore.WarehouseBackupValidationResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.LiveDataTestUtil;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class DataManagementViewModelTest {

    @Rule
    public final InstantTaskExecutorRule executorRule =
            new InstantTaskExecutorRule();

    private RecordingValidateBackupService validateService;
    private RecordingRestoreBackupService restoreService;
    private RecordingExportService exportService;
    private RecordingShareService shareService;
    private RecordingImportService importService;
    private RecordingCreateBackupService createBackupService;
    private DataManagementViewModel viewModel;

    @Before
    public void setUp() {
        validateService = new RecordingValidateBackupService();
        restoreService = new RecordingRestoreBackupService();
        exportService = new RecordingExportService();
        shareService = new RecordingShareService();
        importService = new RecordingImportService();
        createBackupService = new RecordingCreateBackupService();

        viewModel = new DataManagementViewModel(
                exportService,
                shareService,
                importService,
                createBackupService,
                validateService,
                restoreService,
                () -> "warehouse.csv",
                () -> "warehouse-backup.csv"
        );
    }

    @Test
    public void requestExportDestinationEmitsOneShotRequest()
            throws InterruptedException {
        viewModel.requestExportDestination();

        UiEvent<String> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getDestinationRequest()
                );

        assertEquals(
                DataManagementUiState.Status
                        .SELECTING_DESTINATION,
                state().getStatus()
        );
        assertEquals(
                "warehouse.csv",
                event.getContentIfNotHandled()
        );
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void selectExportDestinationDelegatesToService()
            throws InterruptedException {
        viewModel.requestExportDestination();
        viewModel.onDestinationSelected(
                "content://warehouse.csv"
        );

        assertEquals(
                DataManagementUiState.Status.EXPORTING,
                state().getStatus()
        );
        assertEquals(1, exportService.calls);
        assertEquals(
                "content://warehouse.csv",
                exportService.destinationReference
        );
    }

    @Test
    public void cancelExportDestinationReturnsToIdleWithoutExport()
            throws InterruptedException {
        viewModel.requestExportDestination();
        viewModel.onDestinationSelected(null);

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(0, exportService.calls);
    }

    @Test
    public void successfulExportReturnsToIdleAndEmitsCount()
            throws InterruptedException {
        viewModel.requestExportDestination();
        viewModel.onDestinationSelected(
                "content://warehouse.csv"
        );

        exportService.callback.accept(
                ExportWarehouseItemsResult.success(3)
        );

        UiEvent<Integer> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getExportSuccess()
                );

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(
                Integer.valueOf(3),
                event.getContentIfNotHandled()
        );
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void operationInProgressBlocksAnotherOperation()
            throws InterruptedException {
        viewModel.requestExportDestination();
        viewModel.onDestinationSelected(
                "content://warehouse.csv"
        );

        viewModel.requestImportSource();

        assertEquals(
                DataManagementUiState.Status.EXPORTING,
                state().getStatus()
        );
        assertNull(viewModel.getSourceRequest().getValue());
        assertEquals(0, importService.calls);
    }

    @Test
    public void shareSuccessWaitsForChooserCompletion()
            throws InterruptedException {
        ShareableCsvFile shareableFile =
                new ShareableCsvFile(
                        "content://shared/items.csv",
                        "items.csv",
                        "text/csv",
                        2
                );

        viewModel.shareWarehouseItems();
        shareService.callback.accept(
                ShareWarehouseItemsResult.success(
                        shareableFile
                )
        );

        UiEvent<ShareableCsvFile> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getShareFileReady()
                );

        assertEquals(
                shareableFile,
                event.getContentIfNotHandled()
        );
        assertEquals(
                DataManagementUiState.Status.PREPARING_SHARE,
                state().getStatus()
        );

        viewModel.onShareChooserLaunched();

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
    }

    @Test
    public void importSelectionDelegatesToService()
            throws InterruptedException {
        viewModel.requestImportSource();
        viewModel.onImportSourceSelected(
                "content://warehouse.csv"
        );

        assertEquals(
                DataManagementUiState.Status.IMPORTING,
                state().getStatus()
        );
        assertEquals(1, importService.calls);
        assertEquals(
                "content://warehouse.csv",
                importService.sourceReference
        );
    }

    @Test
    public void cancelImportSourceReturnsToIdleWithoutImport()
            throws InterruptedException {
        viewModel.requestImportSource();
        viewModel.onImportSourceSelected(null);

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(0, importService.calls);
    }

    @Test
    public void backupSelectionDelegatesToService()
            throws InterruptedException {
        viewModel.requestBackupDestination();
        viewModel.onBackupDestinationSelected(
                "content://warehouse-backup.csv"
        );

        assertEquals(
                DataManagementUiState.Status.CREATING_BACKUP,
                state().getStatus()
        );
        assertEquals(1, createBackupService.calls);
        assertEquals(
                "content://warehouse-backup.csv",
                createBackupService.destinationReference
        );
    }

    @Test
    public void requestBackupDestinationUsesSuggestedFileName()
            throws InterruptedException {
        viewModel.requestBackupDestination();

        UiEvent<String> event =
                LiveDataTestUtil.getOrAwaitValue(
                        viewModel.getBackupDestinationRequest()
                );

        assertEquals(
                "warehouse-backup.csv",
                event.getContentIfNotHandled()
        );
        assertNull(event.getContentIfNotHandled());
    }

    @Test
    public void cancelBackupDestinationReturnsToIdleWithoutBackup()
            throws InterruptedException {
        viewModel.requestBackupDestination();
        viewModel.onBackupDestinationSelected(null);

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(0, createBackupService.calls);
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
        assertEquals(1, validateService.calls);
        assertEquals(
                "content://backup",
                validateService.sourceReference
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
        assertEquals(0, validateService.calls);
    }

    @Test
    public void validBackup_requestsConfirmationWithItemCount()
            throws InterruptedException {
        WarehouseItem item = item();

        startValidation();
        validateService.callback.accept(
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
        validateService.callback.accept(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item)
                )
        );

        viewModel.confirmBackupRestore();

        assertEquals(
                DataManagementUiState.Status.RESTORING_BACKUP,
                state().getStatus()
        );
        assertEquals(1, restoreService.calls);
        assertEquals(1, restoreService.items.size());
        assertEquals(item, restoreService.items.get(0));
        assertNotSame(
                validateService.resultItems,
                restoreService.items
        );
    }

    @Test
    public void cancelBackupRestore_discardsConfirmation()
            throws InterruptedException {
        startValidation();
        validateService.callback.accept(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item())
                )
        );

        viewModel.cancelBackupRestore();

        assertEquals(
                DataManagementUiState.Status.IDLE,
                state().getStatus()
        );
        assertEquals(0, restoreService.calls);
    }

    @Test
    public void invalidBackup_exposesErrorWithRowNumber()
            throws InterruptedException {
        startValidation();
        validateService.callback.accept(
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
        validateService.callback.accept(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item())
                )
        );
        viewModel.confirmBackupRestore();

        restoreService.callback.accept(
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
        validateService.callback.accept(
                WarehouseBackupValidationResult.valid(
                        Collections.singletonList(item())
                )
        );
        viewModel.confirmBackupRestore();

        restoreService.callback.accept(
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

    private static final class RecordingValidateBackupService
            extends ValidateWarehouseBackupService {

        private int calls;
        private String sourceReference;
        private Consumer<WarehouseBackupValidationResult> callback;
        private List<WarehouseItem> resultItems;

        private RecordingValidateBackupService() {
            super((sourceReference, callback) -> {
            });
        }

        @Override
        public void validateBackup(
                String sourceReference,
                Consumer<WarehouseBackupValidationResult> callback
        ) {
            calls++;
            this.sourceReference = sourceReference;
            this.callback = result -> {
                resultItems = result.getWarehouseItems();
                callback.accept(result);
            };
        }
    }

    private static final class RecordingExportService
            extends ExportWarehouseItemsService {

        private int calls;
        private String destinationReference;
        private Consumer<ExportWarehouseItemsResult> callback;

        private RecordingExportService() {
            super(
                    new WarehouseItemRepositoryStub(),
                    (destination, items, callback) -> {
                    }
            );
        }

        @Override
        public void exportWarehouseItems(
                String destinationReference,
                Consumer<ExportWarehouseItemsResult> callback
        ) {
            calls++;
            this.destinationReference = destinationReference;
            this.callback = callback;
        }
    }

    private static final class RecordingShareService
            extends ShareWarehouseItemsService {

        private int calls;
        private Consumer<ShareWarehouseItemsResult> callback;

        private RecordingShareService() {
            super(
                    new WarehouseItemRepositoryStub(),
                    (items, fileName, callback) -> {
                    },
                    () -> "share.csv"
            );
        }

        @Override
        public void prepareWarehouseItemsForSharing(
                Consumer<ShareWarehouseItemsResult> callback
        ) {
            calls++;
            this.callback = callback;
        }
    }

    private static final class RecordingImportService
            extends ImportWarehouseItemsService {

        private int calls;
        private String sourceReference;
        private Consumer<ImportWarehouseItemsResult> callback;

        private RecordingImportService() {
            super(
                    (sourceReference, callback) -> {
                    },
                    new WarehouseItemRepositoryStub(),
                    () -> 0L
            );
        }

        @Override
        public void importWarehouseItems(
                String sourceReference,
                Consumer<ImportWarehouseItemsResult> callback
        ) {
            calls++;
            this.sourceReference = sourceReference;
            this.callback = callback;
        }
    }

    private static final class RecordingCreateBackupService
            extends CreateWarehouseBackupService {

        private int calls;
        private String destinationReference;
        private Consumer<CreateWarehouseBackupResult> callback;

        private RecordingCreateBackupService() {
            super(
                    new WarehouseItemRepositoryStub(),
                    (destination, items, callback) -> {
                    }
            );
        }

        @Override
        public void createBackup(
                String destinationReference,
                Consumer<CreateWarehouseBackupResult> callback
        ) {
            calls++;
            this.destinationReference = destinationReference;
            this.callback = callback;
        }
    }

    private static final class RecordingRestoreBackupService
            extends RestoreWarehouseBackupService {

        private int calls;
        private List<WarehouseItem> items;
        private Consumer<RestoreWarehouseBackupResult> callback;

        private RecordingRestoreBackupService() {
            super(new WarehouseItemRepositoryStub());
        }

        @Override
        public void restoreBackup(
                List<WarehouseItem> warehouseItems,
                Consumer<RestoreWarehouseBackupResult> callback
        ) {
            calls++;
            items = warehouseItems;
            this.callback = callback;
        }
    }
}
