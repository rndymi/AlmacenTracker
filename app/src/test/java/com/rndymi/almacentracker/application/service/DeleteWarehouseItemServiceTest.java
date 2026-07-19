package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;

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
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class DeleteWarehouseItemServiceTest {

    @Test
    public void delete_returnsInvalidId_whenIdIsZero() {
        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        DeleteWarehouseItemService service =
                new DeleteWarehouseItemService(repository);

        AtomicReference<DeleteWarehouseItemResult> result =
                new AtomicReference<>();

        service.deleteWarehouseItem(
                0L,
                result::set
        );

        assertNotNull(result.get());

        assertEquals(
                DeleteWarehouseItemResult.Status.INVALID_ID,
                result.get().getStatus()
        );

        assertEquals(0, repository.deleteCalls);
    }

    @Test
    public void delete_returnsInvalidId_whenIdIsNegative() {
        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        DeleteWarehouseItemService service =
                new DeleteWarehouseItemService(repository);

        AtomicReference<DeleteWarehouseItemResult> result =
                new AtomicReference<>();

        service.deleteWarehouseItem(
                -3L,
                result::set
        );

        assertEquals(
                DeleteWarehouseItemResult.Status.INVALID_ID,
                result.get().getStatus()
        );

        assertEquals(0, repository.deleteCalls);
    }

    @Test
    public void delete_delegatesValidIdToRepository() {
        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        DeleteWarehouseItemService service =
                new DeleteWarehouseItemService(repository);

        service.deleteWarehouseItem(
                7L,
                ignored -> {
                }
        );

        assertEquals(1, repository.deleteCalls);
        assertEquals(7L, repository.deletedId);
    }

    @Test
    public void delete_returnsSuccess_whenRepositorySucceeds() {
        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        DeleteWarehouseItemService service =
                new DeleteWarehouseItemService(repository);

        AtomicReference<DeleteWarehouseItemResult> result =
                new AtomicReference<>();

        service.deleteWarehouseItem(
                7L,
                result::set
        );

        repository.deleteCallback.onSuccess();

        assertEquals(
                DeleteWarehouseItemResult.Status.SUCCESS,
                result.get().getStatus()
        );
    }

    @Test
    public void delete_returnsNotFound_whenRepositoryFindsNoRow() {
        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        DeleteWarehouseItemService service =
                new DeleteWarehouseItemService(repository);

        AtomicReference<DeleteWarehouseItemResult> result =
                new AtomicReference<>();

        service.deleteWarehouseItem(
                9L,
                result::set
        );

        repository.deleteCallback.onNotFound();

        assertEquals(
                DeleteWarehouseItemResult.Status.NOT_FOUND,
                result.get().getStatus()
        );
    }

    @Test
    public void delete_returnsPersistenceError_whenRepositoryFails() {
        FakeWarehouseItemRepository repository =
                new FakeWarehouseItemRepository();

        DeleteWarehouseItemService service =
                new DeleteWarehouseItemService(repository);

        RuntimeException failure =
                new RuntimeException("Failure");

        AtomicReference<DeleteWarehouseItemResult> result =
                new AtomicReference<>();

        service.deleteWarehouseItem(
                11L,
                result::set
        );

        repository.deleteCallback.onError(failure);

        assertEquals(
                DeleteWarehouseItemResult.Status.PERSISTENCE_ERROR,
                result.get().getStatus()
        );

        assertSame(
                failure,
                result.get().getCause()
        );
    }

    private static final class FakeWarehouseItemRepository
            implements WarehouseItemRepository {

        private int deleteCalls;
        private long deletedId;
        private WarehouseItemDeleteCallback deleteCallback;

        @Override
        public void deleteById(
                long warehouseItemId,
                WarehouseItemDeleteCallback callback
        ) {
            deleteCalls++;
            deletedId = warehouseItemId;
            deleteCallback = callback;
        }

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                WarehouseItemsDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
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
    }
}
