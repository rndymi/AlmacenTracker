package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsInsertCallback;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import androidx.lifecycle.LiveData;

import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class DeleteWarehouseItemsServiceTest {

    @Test
    public void emptySelectionDoesNotInvokeRepository() {
        FakeRepository repository =
                new FakeRepository();

        DeleteWarehouseItemsService service =
                new DeleteWarehouseItemsService(
                        repository
                );

        AtomicReference<DeleteWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        service.deleteWarehouseItems(
                new LinkedHashSet<>(),
                resultReference::set
        );

        assertNotNull(resultReference.get());

        assertEquals(
                DeleteWarehouseItemsResult.Status
                        .EMPTY_SELECTION,
                resultReference.get().getStatus()
        );

        assertEquals(
                0,
                repository.deleteByIdsCallCount
        );
    }

    @Test
    public void invalidIdDoesNotInvokeRepository() {
        FakeRepository repository =
                new FakeRepository();

        DeleteWarehouseItemsService service =
                new DeleteWarehouseItemsService(
                        repository
                );

        AtomicReference<DeleteWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        Set<Long> ids =
                new LinkedHashSet<>(
                        Arrays.asList(
                                1L,
                                0L,
                                2L
                        )
                );

        service.deleteWarehouseItems(
                ids,
                resultReference::set
        );

        assertEquals(
                DeleteWarehouseItemsResult.Status
                        .INVALID_IDS,
                resultReference.get().getStatus()
        );

        assertEquals(
                0,
                repository.deleteByIdsCallCount
        );
    }

    @Test
    public void fullDeletionReturnsSuccess() {
        FakeRepository repository =
                new FakeRepository();

        repository.deletedCount = 3;

        DeleteWarehouseItemsService service =
                new DeleteWarehouseItemsService(
                        repository
                );

        AtomicReference<DeleteWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        service.deleteWarehouseItems(
                setOf(1L, 2L, 3L),
                resultReference::set
        );

        DeleteWarehouseItemsResult result =
                resultReference.get();

        assertEquals(
                DeleteWarehouseItemsResult.Status.SUCCESS,
                result.getStatus()
        );

        assertEquals(3, result.getRequestedCount());
        assertEquals(3, result.getDeletedCount());
        assertEquals(1, repository.deleteByIdsCallCount);
    }

    @Test
    public void partialDeletionReturnsPartialSuccess() {
        FakeRepository repository =
                new FakeRepository();

        repository.deletedCount = 2;

        DeleteWarehouseItemsService service =
                new DeleteWarehouseItemsService(
                        repository
                );

        AtomicReference<DeleteWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        service.deleteWarehouseItems(
                setOf(1L, 2L, 3L),
                resultReference::set
        );

        DeleteWarehouseItemsResult result =
                resultReference.get();

        assertEquals(
                DeleteWarehouseItemsResult.Status
                        .PARTIAL_SUCCESS,
                result.getStatus()
        );

        assertEquals(3, result.getRequestedCount());
        assertEquals(2, result.getDeletedCount());
    }

    @Test
    public void zeroDeletedRowsReturnsNotFound() {
        FakeRepository repository =
                new FakeRepository();

        repository.deletedCount = 0;

        DeleteWarehouseItemsService service =
                new DeleteWarehouseItemsService(
                        repository
                );

        AtomicReference<DeleteWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        service.deleteWarehouseItems(
                setOf(1L, 2L),
                resultReference::set
        );

        assertEquals(
                DeleteWarehouseItemsResult.Status
                        .NOT_FOUND,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void repositoryFailureReturnsPersistenceError() {
        FakeRepository repository =
                new FakeRepository();

        repository.failure =
                new IllegalStateException(
                        "Database failure"
                );

        DeleteWarehouseItemsService service =
                new DeleteWarehouseItemsService(
                        repository
                );

        AtomicReference<DeleteWarehouseItemsResult>
                resultReference =
                new AtomicReference<>();

        service.deleteWarehouseItems(
                setOf(1L, 2L),
                resultReference::set
        );

        DeleteWarehouseItemsResult result =
                resultReference.get();

        assertEquals(
                DeleteWarehouseItemsResult.Status
                        .PERSISTENCE_ERROR,
                result.getStatus()
        );

        assertEquals(
                repository.failure,
                result.getCause()
        );
    }

    private static Set<Long> setOf(Long... ids) {
        return new LinkedHashSet<>(
                Arrays.asList(ids)
        );
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        int deleteByIdsCallCount;
        int deletedCount;
        RuntimeException failure;

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                WarehouseItemsDeleteCallback callback
        ) {
            deleteByIdsCallCount++;

            if (failure != null) {
                callback.onError(failure);
                return;
            }

            callback.onComplete(deletedCount);
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
        public void findAll(
                WarehouseItemsFindCallback callback
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
                com.rndymi.almacentracker.application.port.out
                        .WarehouseItemsReplaceCallback callback
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
    }
}
