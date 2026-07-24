package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.rndymi.almacentracker.application.port.out.RepositoryCallback;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;

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
            extends WarehouseItemRepositoryStub {

        int deleteByIdsCallCount;
        int deletedCount;
        RuntimeException failure;

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                RepositoryCallback<Integer> callback
        ) {
            deleteByIdsCallCount++;

            if (failure != null) {
                callback.onError(failure);
                return;
            }

            callback.onSuccess(deletedCount);
        }

    }
}
