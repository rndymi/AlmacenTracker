package com.rndymi.almacentracker.feature.data_management.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvExportCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvExporter;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsReplaceCallback;
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

public final class ExportWarehouseItemsServiceTest {

    private FakeRepository repository;
    private FakeCsvExporter exporter;
    private ExportWarehouseItemsService service;

    @Before
    public void setUp() {
        repository = new FakeRepository();
        exporter = new FakeCsvExporter();
        service = new ExportWarehouseItemsService(
                repository,
                exporter
        );
    }

    @Test
    public void nullDestinationDoesNotReadRepository() {
        ExportWarehouseItemsResult result = export(null);

        assertEquals(
                ExportWarehouseItemsResult.Status
                        .INVALID_DESTINATION,
                result.getStatus()
        );
        assertEquals(0, repository.findAllCalls);
        assertFalse(exporter.exportCalled);
    }

    @Test
    public void blankDestinationDoesNotReadRepository() {
        ExportWarehouseItemsResult result = export("   ");

        assertEquals(
                ExportWarehouseItemsResult.Status
                        .INVALID_DESTINATION,
                result.getStatus()
        );
        assertEquals(0, repository.findAllCalls);
        assertFalse(exporter.exportCalled);
    }

    @Test
    public void emptyDatabaseDoesNotInvokeExporter() {
        repository.items = Collections.emptyList();

        ExportWarehouseItemsResult result =
                export("content://exports/items.csv");

        assertEquals(
                ExportWarehouseItemsResult.Status.EMPTY_DATABASE,
                result.getStatus()
        );
        assertFalse(exporter.exportCalled);
    }

    @Test
    public void nullRepositoryResultIsTreatedAsEmptyDatabase() {
        repository.items = null;

        ExportWarehouseItemsResult result =
                export("content://exports/items.csv");

        assertEquals(
                ExportWarehouseItemsResult.Status.EMPTY_DATABASE,
                result.getStatus()
        );
        assertFalse(exporter.exportCalled);
    }

    @Test
    public void successfulExportReturnsCountAndDelegatesData() {
        repository.items = List.of(
                item(1L, "MR", "1050"),
                item(2L, "MD", "2050")
        );

        String destination = "content://exports/items.csv";
        ExportWarehouseItemsResult result = export(destination);

        assertEquals(
                ExportWarehouseItemsResult.Status.SUCCESS,
                result.getStatus()
        );
        assertEquals(2, result.getExportedCount());
        assertTrue(exporter.exportCalled);
        assertEquals(destination, exporter.receivedDestination);
        assertEquals(repository.items, exporter.receivedItems);
    }

    @Test
    public void repositoryErrorReturnsReadError() {
        repository.readError =
                new IllegalStateException("Room error");

        ExportWarehouseItemsResult result =
                export("content://exports/items.csv");

        assertEquals(
                ExportWarehouseItemsResult.Status.READ_ERROR,
                result.getStatus()
        );
        assertFalse(exporter.exportCalled);
    }

    @Test
    public void exporterInvalidDestinationIsMapped() {
        exporter.result =
                FakeCsvExporter.Result.INVALID_DESTINATION;

        assertExportStatus(
                ExportWarehouseItemsResult.Status
                        .INVALID_DESTINATION
        );
    }

    @Test
    public void serializationErrorIsMapped() {
        exporter.result =
                FakeCsvExporter.Result.SERIALIZATION_ERROR;

        assertExportStatus(
                ExportWarehouseItemsResult.Status
                        .SERIALIZATION_ERROR
        );
    }

    @Test
    public void writeErrorIsMapped() {
        exporter.result = FakeCsvExporter.Result.WRITE_ERROR;

        assertExportStatus(
                ExportWarehouseItemsResult.Status.WRITE_ERROR
        );
    }

    @Test
    public void unknownErrorIsMapped() {
        exporter.result = FakeCsvExporter.Result.UNKNOWN_ERROR;

        assertExportStatus(
                ExportWarehouseItemsResult.Status.UNKNOWN_ERROR
        );
    }

    private void assertExportStatus(
            ExportWarehouseItemsResult.Status expectedStatus
    ) {
        repository.items = Collections.singletonList(
                item(1L, "MR", "1050")
        );

        ExportWarehouseItemsResult result =
                export("content://exports/items.csv");

        assertEquals(expectedStatus, result.getStatus());
        assertTrue(exporter.exportCalled);
    }

    private ExportWarehouseItemsResult export(
            String destinationReference
    ) {
        AtomicReference<ExportWarehouseItemsResult>
                resultReference = new AtomicReference<>();

        service.exportWarehouseItems(
                destinationReference,
                resultReference::set
        );

        ExportWarehouseItemsResult result =
                resultReference.get();

        assertNotNull(result);
        return result;
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

    private static final class FakeCsvExporter
            implements WarehouseItemCsvExporter {

        private enum Result {
            SUCCESS,
            INVALID_DESTINATION,
            SERIALIZATION_ERROR,
            WRITE_ERROR,
            UNKNOWN_ERROR
        }

        private Result result = Result.SUCCESS;
        private boolean exportCalled;
        private String receivedDestination;
        private List<WarehouseItem> receivedItems =
                Collections.emptyList();

        @Override
        public void export(
                String destinationReference,
                List<WarehouseItem> warehouseItems,
                WarehouseItemCsvExportCallback callback
        ) {
            exportCalled = true;
            receivedDestination = destinationReference;
            receivedItems = new ArrayList<>(warehouseItems);

            switch (result) {
                case SUCCESS:
                    callback.onSuccess();
                    break;

                case INVALID_DESTINATION:
                    callback.onInvalidDestination();
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
        public void replaceAll(
                List<WarehouseItem> warehouseItems,
                WarehouseItemsReplaceCallback callback
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
