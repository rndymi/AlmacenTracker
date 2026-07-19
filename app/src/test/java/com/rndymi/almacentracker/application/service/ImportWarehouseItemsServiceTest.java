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
import com.rndymi.almacentracker.application.result.ImportIssueType;
import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;
import com.rndymi.almacentracker.application.result.WarehouseItemCsvRow;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.ArrayList;
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
                                        2,
                                        " mr ",
                                        " 1050 ",
                                        " A2 "
                                ),
                                row(
                                        3,
                                        " md ",
                                        " 1050 ",
                                        " b1 "
                                ),
                                row(
                                        4,
                                        "MD",
                                        "1050",
                                        "B2"
                                ),
                                row(
                                        5,
                                        "",
                                        "3000",
                                        "C1"
                                )
                        ),
                        4,
                        Collections.emptyList()
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


    @Test
    public void importReportsEveryMissingRequiredFieldWithoutInflatingInvalidRows() {
        FakeRepository repository = new FakeRepository();

        WarehouseItemCsvReadResult csvResult =
                new WarehouseItemCsvReadResult(
                        Collections.singletonList(
                                row(
                                        2,
                                        "",
                                        "",
                                        ""
                                )
                        ),
                        1,
                        Collections.emptyList()
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
                ImportWarehouseItemsResult.Status.NO_VALID_ROWS,
                result.getStatus()
        );

        assertEquals(3, result.getIssueCount());
        assertEquals(1, result.getInvalidCount());
        assertEquals(0, result.getDuplicateCount());

        assertEquals(
                ImportIssueType.MISSING_CATEGORY,
                result.getIssues().get(0).getType()
        );

        assertEquals(
                ImportIssueType.MISSING_CODE,
                result.getIssues().get(1).getType()
        );

        assertEquals(
                ImportIssueType.MISSING_SITE,
                result.getIssues().get(2).getType()
        );
    }

    @Test
    public void importDistinguishesExistingAndInternalDuplicates() {
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
                                row(2, "MR", "1050", "A2"),
                                row(3, "MD", "2000", "B1"),
                                row(4, " md ", " 2000 ", "B2")
                        ),
                        3,
                        Collections.emptyList()
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
                ImportWarehouseItemsResult.Status.PARTIAL_SUCCESS,
                result.getStatus()
        );

        assertEquals(1, result.getImportedCount());
        assertEquals(2, result.getDuplicateCount());
        assertEquals(0, result.getInvalidCount());

        assertEquals(
                ImportIssueType.DUPLICATE_EXISTING,
                result.getIssues().get(0).getType()
        );

        assertEquals(
                ImportIssueType.DUPLICATE_IN_FILE,
                result.getIssues().get(1).getType()
        );

        assertEquals(
                Integer.valueOf(3),
                result.getIssues()
                        .get(1)
                        .getRelatedRowNumber()
        );
    }

    @Test
    public void invalidRowIsNotAlsoClassifiedAsDuplicate() {
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
                        Collections.singletonList(
                                row(
                                        2,
                                        "MR",
                                        "1050",
                                        ""
                                )
                        ),
                        1,
                        Collections.emptyList()
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

        assertEquals(1, result.getInvalidCount());
        assertEquals(0, result.getDuplicateCount());
        assertEquals(1, result.getIssues().size());

        assertEquals(
                ImportIssueType.MISSING_SITE,
                result.getIssues().get(0).getType()
        );
    }

    @Test
    public void persistenceFailureReportsZeroImportedRows() {
        FakeRepository repository = new FakeRepository();
        repository.insertError =
                new IllegalStateException("Insert failed");

        WarehouseItemCsvReadResult csvResult =
                new WarehouseItemCsvReadResult(
                        Collections.singletonList(
                                row(
                                        2,
                                        "MR",
                                        "1050",
                                        "A1"
                                )
                        ),
                        1,
                        Collections.emptyList()
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
                        .PERSISTENCE_ERROR,
                result.getStatus()
        );

        assertEquals(0, result.getImportedCount());
    }

    private static WarehouseItemCsvRow row(
            int rowNumber,
            String category,
            String code,
            String site
    ) {
        return new WarehouseItemCsvRow(
                rowNumber,
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

        private Throwable insertError;

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
            if (insertError != null) {
                callback.onError(insertError);
                return;
            }

            insertedItems =
                    new ArrayList<>(warehouseItems);

            callback.onSuccess(
                    insertedItems.size()
            );
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