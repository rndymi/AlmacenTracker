package com.rndymi.almacentracker.feature.data_management.backup.create;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.application.port.out.RepositoryCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExportCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExporter;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class CreateWarehouseBackupServiceTest {

    private FakeRepository repository;
    private FakeBackupExporter exporter;
    private CreateWarehouseBackupService service;

    @Before
    public void setUp() {
        repository = new FakeRepository();
        exporter = new FakeBackupExporter();

        service = new CreateWarehouseBackupService(
                repository,
                exporter
        );
    }

    @Test
    public void invalidDestinationDoesNotReadRepository() {
        AtomicReference<CreateWarehouseBackupResult>
                resultReference =
                new AtomicReference<>();

        service.createBackup(
                "   ",
                resultReference::set
        );

        assertEquals(
                CreateWarehouseBackupResult.Status
                        .INVALID_DESTINATION,
                resultReference.get().getStatus()
        );

        assertEquals(0, repository.findAllCalls);
    }

    @Test
    public void emptyDatabaseCreatesValidBackup() {
        repository.items = Collections.emptyList();

        AtomicReference<CreateWarehouseBackupResult>
                resultReference =
                new AtomicReference<>();

        service.createBackup(
                "content://backup",
                resultReference::set
        );

        assertEquals(
                CreateWarehouseBackupResult.Status.SUCCESS,
                resultReference.get().getStatus()
        );

        assertEquals(
                0,
                resultReference.get().getBackedUpCount()
        );

        assertTrue(exporter.exportCalled);
        assertTrue(exporter.receivedItems.isEmpty());
    }

    @Test
    public void successReturnsBackedUpCount() {
        repository.items = List.of(
                item(1L, "MR", "1050"),
                item(2L, "MD", "1050")
        );

        AtomicReference<CreateWarehouseBackupResult>
                resultReference =
                new AtomicReference<>();

        service.createBackup(
                "content://backup",
                resultReference::set
        );

        assertEquals(
                CreateWarehouseBackupResult.Status.SUCCESS,
                resultReference.get().getStatus()
        );

        assertEquals(
                2,
                resultReference.get().getBackedUpCount()
        );
    }

    @Test
    public void repositoryErrorReturnsReadError() {
        repository.readError =
                new IllegalStateException("Room error");

        AtomicReference<CreateWarehouseBackupResult>
                resultReference =
                new AtomicReference<>();

        service.createBackup(
                "content://backup",
                resultReference::set
        );

        assertEquals(
                CreateWarehouseBackupResult.Status.READ_ERROR,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void invalidDataIsMapped() {
        exporter.result =
                FakeBackupExporter.Result.INVALID_DATA;

        AtomicReference<CreateWarehouseBackupResult>
                resultReference =
                new AtomicReference<>();

        service.createBackup(
                "content://backup",
                resultReference::set
        );

        assertEquals(
                CreateWarehouseBackupResult.Status.INVALID_DATA,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void writeErrorIsMapped() {
        exporter.result =
                FakeBackupExporter.Result.WRITE_ERROR;

        AtomicReference<CreateWarehouseBackupResult>
                resultReference =
                new AtomicReference<>();

        service.createBackup(
                "content://backup",
                resultReference::set
        );

        assertEquals(
                CreateWarehouseBackupResult.Status.WRITE_ERROR,
                resultReference.get().getStatus()
        );
    }

    private WarehouseItem item(
            long id,
            String category,
            String code
    ) {
        return new WarehouseItem(
                id,
                category,
                code,
                "A1",
                null,
                null,
                1000L,
                2000L
        );
    }

    private static final class FakeBackupExporter
            implements WarehouseBackupCsvExporter {

        private enum Result {
            SUCCESS,
            INVALID_DESTINATION,
            INVALID_DATA,
            SERIALIZATION_ERROR,
            WRITE_ERROR,
            UNKNOWN_ERROR
        }

        private Result result = Result.SUCCESS;
        private boolean exportCalled;
        private List<WarehouseItem> receivedItems =
                Collections.emptyList();

        @Override
        public void exportBackup(
                String destinationReference,
                List<WarehouseItem> warehouseItems,
                WarehouseBackupCsvExportCallback callback
        ) {
            exportCalled = true;
            receivedItems = new ArrayList<>(warehouseItems);

            switch (result) {
                case SUCCESS:
                    callback.onSuccess();
                    break;

                case INVALID_DESTINATION:
                    callback.onInvalidDestination();
                    break;

                case INVALID_DATA:
                    callback.onInvalidData(
                            new IllegalArgumentException()
                    );
                    break;

                case SERIALIZATION_ERROR:
                    callback.onSerializationError(
                            new IllegalStateException()
                    );
                    break;

                case WRITE_ERROR:
                    callback.onWriteError(
                            new IllegalStateException()
                    );
                    break;

                case UNKNOWN_ERROR:
                    callback.onUnknownError(
                            new IllegalStateException()
                    );
                    break;
            }
        }
    }

    private static final class FakeRepository
            extends WarehouseItemRepositoryStub {

        private List<WarehouseItem> items =
                Collections.emptyList();

        private Throwable readError;
        private int findAllCalls;

        @Override
        public void findAll(
                RepositoryCallback<List<WarehouseItem>> callback
        ) {
            findAllCalls++;

            if (readError != null) {
                callback.onError(readError);
                return;
            }

            callback.onSuccess(items);
        }

    }
}
