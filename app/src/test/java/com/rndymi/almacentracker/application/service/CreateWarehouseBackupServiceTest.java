package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExportCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseBackupCsvExporter;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsInsertCallback;
import com.rndymi.almacentracker.application.result.CreateWarehouseBackupResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
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

    /*
     * Conserva aquí el mismo FakeRepository completo que ya
     * utiliza ImportWarehouseItemsServiceTest, modificando
     * únicamente findAll().
     */
    private static final class FakeRepository
            implements WarehouseItemRepository {

        private List<WarehouseItem> items =
                Collections.emptyList();

        private Throwable readError;
        private int findAllCalls;

        @Override
        public void findAll(
                WarehouseItemsFindCallback callback
        ) {
            findAllCalls++;

            if (readError != null) {
                callback.onError(readError);
                return;
            }

            callback.onSuccess(items);
        }

        @Override
        public LiveData<WarehouseItemsResult> observeAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemsResult> search(
                String query
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemsResult> filter(
                WarehouseItemFilterCriteria criteria
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemFilterOptionsResult>
        observeFilterOptions() {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemDetailResult> observeById(
                long warehouseItemId
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void findById(
                long warehouseItemId,
                WarehouseItemFindCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void existsByCategoryAndCode(
                String category,
                String code,
                WarehouseItemDuplicateCheckCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void existsByCategoryAndCodeExcludingId(
                String category,
                String code,
                long excludedWarehouseItemId,
                WarehouseItemDuplicateCheckCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insert(
                WarehouseItem warehouseItem,
                WarehouseItemInsertCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void insertAll(
                List<WarehouseItem> warehouseItems,
                WarehouseItemsInsertCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void update(
                WarehouseItem warehouseItem,
                WarehouseItemUpdateCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteById(
                long warehouseItemId,
                WarehouseItemDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                WarehouseItemsDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }
    }
}
