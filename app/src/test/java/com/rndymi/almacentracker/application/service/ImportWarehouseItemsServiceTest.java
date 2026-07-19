package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReadCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemCsvReader;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsInsertCallback;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class ImportWarehouseItemsServiceTest {

    @Test
    public void importNormalizesSkipsDuplicatesAndInsertsBatch() {
        FakeRepository repository = new FakeRepository();

        repository.existingItems =
                Collections.singletonList(
                        item(
                                1L,
                                "MR",
                                "1050",
                                "A1"
                        )
                );

        WarehouseItemCsvReadResult csvResult =
                new WarehouseItemCsvReadResult(
                        Arrays.asList(
                                row(
                                        " mr ",
                                        " 1050 ",
                                        " A2 "
                                ),
                                row(
                                        " md ",
                                        " 1050 ",
                                        " b1 "
                                ),
                                row(
                                        "MD",
                                        "1050",
                                        "B2"
                                ),
                                row(
                                        "",
                                        "3000",
                                        "C1"
                                )
                        ),
                        4,
                        0
                );

        ImportWarehouseItemsService service =
                new ImportWarehouseItemsService(
                        new SuccessfulReader(csvResult),
                        repository,
                        () -> 1000L
                );

        AtomicReference<ImportWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        service.importWarehouseItems(
                "content://test/import.csv",
                resultReference::set
        );

        ImportWarehouseItemsResult result =
                resultReference.get();

        assertEquals(
                ImportWarehouseItemsResult.Status
                        .PARTIAL_SUCCESS,
                result.getStatus()
        );

        assertEquals(1, result.getImportedCount());
        assertEquals(2, result.getDuplicateCount());
        assertEquals(1, result.getInvalidCount());

        assertEquals(1, repository.insertedItems.size());

        WarehouseItem inserted =
                repository.insertedItems.get(0);

        assertEquals("MD", inserted.getCategory());
        assertEquals("1050", inserted.getCode());
        assertEquals("B1", inserted.getSite());
        assertEquals(1000L, inserted.getCreatedAt());
        assertEquals(1000L, inserted.getUpdatedAt());
        assertNull(inserted.getPosition());
    }

    private static WarehouseItemCsvRow row(
            String category,
            String code,
            String site
    ) {
        return new WarehouseItemCsvRow(
                category,
                code,
                site,
                "",
                ""
        );
    }

    private static WarehouseItem item(
            long id,
            String category,
            String code,
            String site
    ) {
        return new WarehouseItem(
                id,
                category,
                code,
                site,
                null,
                null,
                1L,
                1L
        );
    }

    private static final class SuccessfulReader
            implements WarehouseItemCsvReader {

        private final WarehouseItemCsvReadResult result;

        private SuccessfulReader(
                WarehouseItemCsvReadResult result
        ) {
            this.result = result;
        }

        @Override
        public void read(
                String sourceReference,
                WarehouseItemCsvReadCallback callback
        ) {
            callback.onSuccess(result);
        }
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        private List<WarehouseItem> existingItems =
                Collections.emptyList();

        private List<WarehouseItem> insertedItems =
                Collections.emptyList();

        @Override
        public void findAll(
                WarehouseItemsFindCallback callback
        ) {
            callback.onSuccess(existingItems);
        }

        @Override
        public void insertAll(
                List<WarehouseItem> warehouseItems,
                WarehouseItemsInsertCallback callback
        ) {
            insertedItems = warehouseItems;
            callback.onSuccess(warehouseItems.size());
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