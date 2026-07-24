package com.rndymi.almacentracker.feature.inventory.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class WarehouseItemDeleteServiceTest {

    @Test
    public void singleDeleteRejectsInvalidId() {
        AtomicReference<WarehouseItemDeleteResult> zeroResult =
                new AtomicReference<>();
        AtomicReference<WarehouseItemDeleteResult> negativeResult =
                new AtomicReference<>();

        WarehouseItemDeleteService service =
                service(new WarehouseItemRepositoryStub());

        service.delete(0L, zeroResult::set);
        service.delete(-1L, negativeResult::set);

        assertEquals(
                WarehouseItemDeleteResult.Status.INVALID_ID,
                zeroResult.get().getStatus()
        );
        assertEquals(
                WarehouseItemDeleteResult.Status.INVALID_ID,
                negativeResult.get().getStatus()
        );
    }

    @Test
    public void singleDeleteReturnsSuccess() {
        AtomicReference<Long> deletedId =
                new AtomicReference<>();

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void deleteById(
                            long warehouseItemId,
                            RepositoryCallback<Void> callback
                    ) {
                        deletedId.set(warehouseItemId);
                        callback.onSuccess(null);
                    }
                };

        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repository).delete(8L, result::set);

        assertEquals(Long.valueOf(8L), deletedId.get());
        assertEquals(
                WarehouseItemDeleteResult.Status.SUCCESS,
                result.get().getStatus()
        );
        assertEquals(1, result.get().getDeletedCount());
    }

    @Test
    public void singleDeleteReturnsNotFound() {
        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void deleteById(
                            long warehouseItemId,
                            RepositoryCallback<Void> callback
                    ) {
                        callback.onNotFound();
                    }
                };

        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repository).delete(8L, result::set);

        assertEquals(
                WarehouseItemDeleteResult.Status.NOT_FOUND,
                result.get().getStatus()
        );
    }

    @Test
    public void singleDeleteReturnsPersistenceError() {
        IllegalStateException failure =
                new IllegalStateException("delete");

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void deleteById(
                            long warehouseItemId,
                            RepositoryCallback<Void> callback
                    ) {
                        callback.onError(failure);
                    }
                };

        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repository).delete(8L, result::set);

        assertEquals(
                WarehouseItemDeleteResult.Status.PERSISTENCE_ERROR,
                result.get().getStatus()
        );
        assertSame(failure, result.get().getCause());
    }

    @Test
    public void multipleDeleteRejectsEmptyOrInvalidSelection() {
        WarehouseItemDeleteService service =
                service(new WarehouseItemRepositoryStub());

        AtomicReference<WarehouseItemDeleteResult> emptyResult =
                new AtomicReference<>();
        AtomicReference<WarehouseItemDeleteResult> invalidResult =
                new AtomicReference<>();

        service.delete(Set.of(), emptyResult::set);
        service.delete(Set.of(1L, 0L), invalidResult::set);

        assertEquals(
                WarehouseItemDeleteResult.Status.EMPTY_SELECTION,
                emptyResult.get().getStatus()
        );
        assertEquals(
                WarehouseItemDeleteResult.Status.INVALID_ID,
                invalidResult.get().getStatus()
        );
        assertEquals(2, invalidResult.get().getRequestedCount());
    }

    @Test
    public void multipleDeleteReturnsFullSuccess() {
        AtomicReference<List<Long>> deletedIds =
                new AtomicReference<>();

        WarehouseItemRepositoryStub repository =
                repositoryDeleting(
                        deletedIds,
                        2
                );

        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repository).delete(
                Set.of(3L, 7L),
                result::set
        );

        assertEquals(Set.of(3L, 7L), Set.copyOf(deletedIds.get()));
        assertEquals(
                WarehouseItemDeleteResult.Status.SUCCESS,
                result.get().getStatus()
        );
        assertEquals(2, result.get().getRequestedCount());
        assertEquals(2, result.get().getDeletedCount());
    }

    @Test
    public void multipleDeleteReturnsPartialSuccess() {
        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repositoryDeleting(null, 1)).delete(
                Set.of(3L, 7L),
                result::set
        );

        assertEquals(
                WarehouseItemDeleteResult.Status.PARTIAL_SUCCESS,
                result.get().getStatus()
        );
        assertEquals(2, result.get().getRequestedCount());
        assertEquals(1, result.get().getDeletedCount());
    }

    @Test
    public void multipleDeleteReturnsNotFound() {
        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repositoryDeleting(null, 0)).delete(
                Set.of(3L, 7L),
                result::set
        );

        assertEquals(
                WarehouseItemDeleteResult.Status.NOT_FOUND,
                result.get().getStatus()
        );
    }

    @Test
    public void multipleDeleteReturnsPersistenceError() {
        IllegalStateException failure =
                new IllegalStateException("delete");

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void deleteByIds(
                            List<Long> warehouseItemIds,
                            RepositoryCallback<Integer> callback
                    ) {
                        callback.onError(failure);
                    }
                };

        AtomicReference<WarehouseItemDeleteResult> result =
                new AtomicReference<>();

        service(repository).delete(
                Set.of(3L, 7L),
                result::set
        );

        assertEquals(
                WarehouseItemDeleteResult.Status.PERSISTENCE_ERROR,
                result.get().getStatus()
        );
        assertEquals(2, result.get().getRequestedCount());
        assertSame(failure, result.get().getCause());
    }

    private WarehouseItemDeleteService service(
            WarehouseItemRepositoryStub repository
    ) {
        return new WarehouseItemDeleteService(repository);
    }

    private WarehouseItemRepositoryStub repositoryDeleting(
            AtomicReference<List<Long>> deletedIds,
            int deletedCount
    ) {
        return new WarehouseItemRepositoryStub() {
            @Override
            public void deleteByIds(
                    List<Long> warehouseItemIds,
                    RepositoryCallback<Integer> callback
            ) {
                if (deletedIds != null) {
                    deletedIds.set(warehouseItemIds);
                }

                callback.onSuccess(deletedCount);
            }
        };
    }
}
