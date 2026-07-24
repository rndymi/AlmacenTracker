package com.rndymi.almacentracker.feature.inventory.form;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.domain.model.WarehouseItem;
import com.rndymi.almacentracker.testutil.WarehouseItemRepositoryStub;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

public final class WarehouseItemSaveServiceTest {

    @Test
    public void createNormalizesAndInsertsWarehouseItem() {
        AtomicReference<WarehouseItem> inserted =
                new AtomicReference<>();

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void existsByCategoryAndCode(
                            String category,
                            String code,
                            RepositoryCallback<Boolean> callback
                    ) {
                        assertEquals("MR", category);
                        assertEquals("1050", code);
                        callback.onSuccess(false);
                    }

                    @Override
                    public void insert(
                            WarehouseItem warehouseItem,
                            RepositoryCallback<Long> callback
                    ) {
                        inserted.set(warehouseItem);
                        callback.onSuccess(17L);
                    }
                };

        AtomicReference<WarehouseItemSaveResult> result =
                new AtomicReference<>();

        service(repository).create(
                formData(),
                result::set
        );

        WarehouseItem item = inserted.get();
        assertEquals(0L, item.getId());
        assertEquals("MR", item.getCategory());
        assertEquals("1050", item.getCode());
        assertEquals("A1", item.getSite());
        assertEquals("Nivel 2", item.getPosition());
        assertEquals("Observación", item.getObservations());
        assertEquals(200L, item.getCreatedAt());
        assertEquals(200L, item.getUpdatedAt());
        assertEquals(
                WarehouseItemSaveResult.Status.SUCCESS,
                result.get().getStatus()
        );
        assertEquals(17L, result.get().getWarehouseItemId());
    }

    @Test
    public void createTurnsEmptyOptionalFieldsIntoNull() {
        AtomicReference<WarehouseItem> inserted =
                new AtomicReference<>();

        WarehouseItemRepositoryStub repository =
                repositoryForCreate(inserted);

        service(repository).create(
                new WarehouseItemFormData(
                        "MR",
                        "1050",
                        "A1",
                        " ",
                        null
                ),
                ignored -> {
                }
        );

        assertNull(inserted.get().getPosition());
        assertNull(inserted.get().getObservations());
    }

    @Test
    public void createValidatesBeforeAccessingRepository() {
        AtomicReference<WarehouseItemSaveResult> result =
                new AtomicReference<>();

        service(new WarehouseItemRepositoryStub()).create(
                new WarehouseItemFormData(
                        " ",
                        null,
                        "",
                        null,
                        null
                ),
                result::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.VALIDATION_ERROR,
                result.get().getStatus()
        );
        assertTrue(result.get().isCategoryRequired());
        assertTrue(result.get().isCodeRequired());
        assertTrue(result.get().isSiteRequired());
    }

    @Test
    public void createReturnsDuplicateFromEitherProtection() {
        WarehouseItemRepositoryStub existingRepository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void existsByCategoryAndCode(
                            String category,
                            String code,
                            RepositoryCallback<Boolean> callback
                    ) {
                        callback.onSuccess(true);
                    }
                };

        AtomicReference<WarehouseItemSaveResult> existingResult =
                new AtomicReference<>();

        service(existingRepository).create(
                formData(),
                existingResult::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.DUPLICATE,
                existingResult.get().getStatus()
        );

        WarehouseItemRepositoryStub roomRepository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void existsByCategoryAndCode(
                            String category,
                            String code,
                            RepositoryCallback<Boolean> callback
                    ) {
                        callback.onSuccess(false);
                    }

                    @Override
                    public void insert(
                            WarehouseItem warehouseItem,
                            RepositoryCallback<Long> callback
                    ) {
                        callback.onDuplicate(
                                new IllegalStateException("duplicate")
                        );
                    }
                };

        AtomicReference<WarehouseItemSaveResult> roomResult =
                new AtomicReference<>();

        service(roomRepository).create(
                formData(),
                roomResult::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.DUPLICATE,
                roomResult.get().getStatus()
        );
    }

    @Test
    public void createReturnsPersistenceError() {
        IllegalStateException failure =
                new IllegalStateException("lookup");

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void existsByCategoryAndCode(
                            String category,
                            String code,
                            RepositoryCallback<Boolean> callback
                    ) {
                        callback.onError(failure);
                    }
                };

        AtomicReference<WarehouseItemSaveResult> result =
                new AtomicReference<>();

        service(repository).create(formData(), result::set);

        assertEquals(
                WarehouseItemSaveResult.Status.PERSISTENCE_ERROR,
                result.get().getStatus()
        );
        assertSame(failure, result.get().getCause());
    }

    @Test
    public void updatePreservesIdentityAndCreationTime() {
        WarehouseItem original = originalItem();
        AtomicReference<WarehouseItem> updated =
                new AtomicReference<>();

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void findById(
                            long warehouseItemId,
                            RepositoryCallback<WarehouseItem> callback
                    ) {
                        assertEquals(8L, warehouseItemId);
                        callback.onSuccess(original);
                    }

                    @Override
                    public void existsByCategoryAndCodeExcludingId(
                            String category,
                            String code,
                            long excludedWarehouseItemId,
                            RepositoryCallback<Boolean> callback
                    ) {
                        assertEquals("MR", category);
                        assertEquals("1050", code);
                        assertEquals(8L, excludedWarehouseItemId);
                        callback.onSuccess(false);
                    }

                    @Override
                    public void update(
                            WarehouseItem warehouseItem,
                            RepositoryCallback<Void> callback
                    ) {
                        updated.set(warehouseItem);
                        callback.onSuccess(null);
                    }
                };

        AtomicReference<WarehouseItemSaveResult> result =
                new AtomicReference<>();

        service(repository).update(
                8L,
                formData(),
                result::set
        );

        WarehouseItem item = updated.get();
        assertEquals(8L, item.getId());
        assertEquals(100L, item.getCreatedAt());
        assertEquals(200L, item.getUpdatedAt());
        assertEquals("MR", item.getCategory());
        assertEquals("1050", item.getCode());
        assertEquals(
                WarehouseItemSaveResult.Status.SUCCESS,
                result.get().getStatus()
        );
        assertEquals(8L, result.get().getWarehouseItemId());
    }

    @Test
    public void updateRejectsInvalidOrMissingWarehouseItem() {
        AtomicReference<WarehouseItemSaveResult> invalidResult =
                new AtomicReference<>();

        service(new WarehouseItemRepositoryStub()).update(
                0L,
                formData(),
                invalidResult::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.NOT_FOUND,
                invalidResult.get().getStatus()
        );

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void findById(
                            long warehouseItemId,
                            RepositoryCallback<WarehouseItem> callback
                    ) {
                        callback.onNotFound();
                    }
                };

        AtomicReference<WarehouseItemSaveResult> missingResult =
                new AtomicReference<>();

        service(repository).update(
                8L,
                formData(),
                missingResult::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.NOT_FOUND,
                missingResult.get().getStatus()
        );
    }

    @Test
    public void updateValidatesBeforeReadingExistingItem() {
        AtomicReference<WarehouseItemSaveResult> result =
                new AtomicReference<>();

        service(new WarehouseItemRepositoryStub()).update(
                8L,
                new WarehouseItemFormData(
                        "",
                        "",
                        "",
                        null,
                        null
                ),
                result::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.VALIDATION_ERROR,
                result.get().getStatus()
        );
        assertTrue(result.get().isCategoryRequired());
        assertTrue(result.get().isCodeRequired());
        assertTrue(result.get().isSiteRequired());
    }

    @Test
    public void updateReturnsDuplicateFromEitherProtection() {
        WarehouseItemRepositoryStub existingRepository =
                repositoryForUpdate(true, false);

        AtomicReference<WarehouseItemSaveResult> existingResult =
                new AtomicReference<>();

        service(existingRepository).update(
                8L,
                formData(),
                existingResult::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.DUPLICATE,
                existingResult.get().getStatus()
        );

        WarehouseItemRepositoryStub roomRepository =
                repositoryForUpdate(false, true);

        AtomicReference<WarehouseItemSaveResult> roomResult =
                new AtomicReference<>();

        service(roomRepository).update(
                8L,
                formData(),
                roomResult::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.DUPLICATE,
                roomResult.get().getStatus()
        );
    }

    @Test
    public void updateReturnsPersistenceError() {
        IllegalStateException failure =
                new IllegalStateException("lookup");

        WarehouseItemRepositoryStub repository =
                new WarehouseItemRepositoryStub() {
                    @Override
                    public void findById(
                            long warehouseItemId,
                            RepositoryCallback<WarehouseItem> callback
                    ) {
                        callback.onSuccess(originalItem());
                    }

                    @Override
                    public void existsByCategoryAndCodeExcludingId(
                            String category,
                            String code,
                            long excludedWarehouseItemId,
                            RepositoryCallback<Boolean> callback
                    ) {
                        callback.onError(failure);
                    }
                };

        AtomicReference<WarehouseItemSaveResult> result =
                new AtomicReference<>();

        service(repository).update(
                8L,
                formData(),
                result::set
        );

        assertEquals(
                WarehouseItemSaveResult.Status.PERSISTENCE_ERROR,
                result.get().getStatus()
        );
        assertSame(failure, result.get().getCause());
    }

    private WarehouseItemSaveService service(
            WarehouseItemRepositoryStub repository
    ) {
        return new WarehouseItemSaveService(
                repository,
                () -> 200L
        );
    }

    private WarehouseItemFormData formData() {
        return new WarehouseItemFormData(
                " mr ",
                " 1050 ",
                " a1 ",
                " Nivel 2 ",
                " Observación "
        );
    }

    private WarehouseItem originalItem() {
        return new WarehouseItem(
                8L,
                "MR",
                "1050",
                "A1",
                null,
                null,
                100L,
                150L
        );
    }

    private WarehouseItemRepositoryStub repositoryForCreate(
            AtomicReference<WarehouseItem> inserted
    ) {
        return new WarehouseItemRepositoryStub() {
            @Override
            public void existsByCategoryAndCode(
                    String category,
                    String code,
                    RepositoryCallback<Boolean> callback
            ) {
                callback.onSuccess(false);
            }

            @Override
            public void insert(
                    WarehouseItem warehouseItem,
                    RepositoryCallback<Long> callback
            ) {
                inserted.set(warehouseItem);
                callback.onSuccess(17L);
            }
        };
    }

    private WarehouseItemRepositoryStub repositoryForUpdate(
            boolean exists,
            boolean roomDuplicate
    ) {
        return new WarehouseItemRepositoryStub() {
            @Override
            public void findById(
                    long warehouseItemId,
                    RepositoryCallback<WarehouseItem> callback
            ) {
                callback.onSuccess(originalItem());
            }

            @Override
            public void existsByCategoryAndCodeExcludingId(
                    String category,
                    String code,
                    long excludedWarehouseItemId,
                    RepositoryCallback<Boolean> callback
            ) {
                callback.onSuccess(exists);
            }

            @Override
            public void update(
                    WarehouseItem warehouseItem,
                    RepositoryCallback<Void> callback
            ) {
                assertFalse(exists);

                if (roomDuplicate) {
                    callback.onDuplicate(
                            new IllegalStateException("duplicate")
                    );
                } else {
                    callback.onSuccess(null);
                }
            }
        };
    }
}
