package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import com.rndymi.almacentracker.application.port.out.RepositoryCallback;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;

import org.junit.Test;

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

        repository.deleteCallback.onSuccess(null);

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
            extends WarehouseItemRepositoryStub {

        private int deleteCalls;
        private long deletedId;
        private RepositoryCallback<Void> deleteCallback;

        @Override
        public void deleteById(
                long warehouseItemId,
                RepositoryCallback<Void> callback
        ) {
            deleteCalls++;
            deletedId = warehouseItemId;
            deleteCallback = callback;
        }

    }
}
