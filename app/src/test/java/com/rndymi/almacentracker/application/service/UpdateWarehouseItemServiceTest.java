package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDuplicateCheckCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsFindCallback;
import com.rndymi.almacentracker.application.result.UpdateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UpdateWarehouseItemServiceTest {

    private static final long ORIGINAL_CREATED_AT = 100L;
    private static final long NEW_UPDATED_AT = 500L;

    @Test
    public void updatePreservesIdAndCreatedAt() {
        FakeRepository repository = new FakeRepository();

        repository.existingItem = new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                "Nivel 1",
                null,
                ORIGINAL_CREATED_AT,
                200L
        );

        UpdateWarehouseItemService service =
                new UpdateWarehouseItemService(
                        repository,
                        () -> NEW_UPDATED_AT
                );

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        " md ",
                        " 2050 ",
                        " b2 ",
                        "",
                        "  Nueva observación  "
                ),
                result::set
        );

        assertEquals(
                UpdateWarehouseItemResult.Status.SUCCESS,
                result.get().getStatus()
        );

        assertEquals(
                7L,
                repository.updatedItem.getId()
        );

        assertEquals(
                ORIGINAL_CREATED_AT,
                repository.updatedItem.getCreatedAt()
        );

        assertEquals(
                NEW_UPDATED_AT,
                repository.updatedItem.getUpdatedAt()
        );

        assertEquals(
                "MD",
                repository.updatedItem.getCategory()
        );

        assertEquals(
                "2050",
                repository.updatedItem.getCode()
        );

        assertEquals(
                "B2",
                repository.updatedItem.getSite()
        );

        assertNull(
                repository.updatedItem.getPosition()
        );

        assertEquals(
                "Nueva observación",
                repository.updatedItem.getObservations()
        );
    }

    @Test
    public void validationFailureDoesNotAccessRepository() {
        FakeRepository repository = new FakeRepository();

        UpdateWarehouseItemService service =
                new UpdateWarehouseItemService(
                        repository,
                        () -> NEW_UPDATED_AT
                );

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        " ",
                        "",
                        " ",
                        null,
                        null
                ),
                result::set
        );

        assertEquals(
                UpdateWarehouseItemResult.Status.VALIDATION_ERROR,
                result.get().getStatus()
        );

        assertTrue(result.get().isCategoryRequired());
        assertTrue(result.get().isCodeRequired());
        assertTrue(result.get().isSiteRequired());
        assertFalse(repository.duplicateCheckCalled);
        assertFalse(repository.findCalled);
        assertNull(repository.updatedItem);
    }

    @Test
    public void missingWarehouseItemReturnsNotFound() {
        FakeRepository repository = new FakeRepository();

        UpdateWarehouseItemService service =
                new UpdateWarehouseItemService(
                        repository,
                        () -> NEW_UPDATED_AT
                );

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        99L,
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null
                ),
                result::set
        );

        assertEquals(
                UpdateWarehouseItemResult.Status.NOT_FOUND,
                result.get().getStatus()
        );

        assertFalse(repository.duplicateCheckCalled);
        assertNull(repository.updatedItem);
    }

    @Test
    public void existingCombinationReturnsDuplicateWithoutUpdate() {
        FakeRepository repository = new FakeRepository();

        repository.existingItem = createExistingItem();
        repository.duplicateExists = true;

        UpdateWarehouseItemService service =
                createService(repository);

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        "MD",
                        "1050",
                        "A1",
                        null,
                        null
                ),
                result::set
        );

        assertTrue(repository.duplicateCheckCalled);
        assertNull(repository.updatedItem);

        assertEquals(
                UpdateWarehouseItemResult.Status.DUPLICATE,
                result.get().getStatus()
        );
    }

    @Test
    public void duplicateCheckUsesNormalizedIdentityAndExcludesOwnId() {
        FakeRepository repository = new FakeRepository();
        repository.existingItem = createExistingItem();

        UpdateWarehouseItemService service =
                createService(repository);

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        "  mr ",
                        " 1050 ",
                        "A2",
                        null,
                        null
                ),
                ignored -> {
                }
        );

        assertTrue(repository.duplicateCheckCalled);
        assertEquals("MR", repository.checkedCategory);
        assertEquals("1050", repository.checkedCode);

        assertEquals(
                7L,
                repository.excludedWarehouseItemId
        );
    }

    @Test
    public void unchangedIdentityCanBeUpdated() {
        FakeRepository repository = new FakeRepository();
        repository.existingItem = createExistingItem();
        repository.duplicateExists = false;

        UpdateWarehouseItemService service =
                createService(repository);

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        "MR",
                        "1050",
                        "B2",
                        "Nivel 3",
                        null
                ),
                result::set
        );

        assertTrue(repository.duplicateCheckCalled);
        assertEquals(
                7L,
                repository.excludedWarehouseItemId
        );

        assertEquals(
                UpdateWarehouseItemResult.Status.SUCCESS,
                result.get().getStatus()
        );

        assertEquals(
                "B2",
                repository.updatedItem.getSite()
        );
    }

    @Test
    public void availableCombinationCanBeUpdated() {
        FakeRepository repository = new FakeRepository();
        repository.existingItem = createExistingItem();
        repository.duplicateExists = false;

        UpdateWarehouseItemService service =
                createService(repository);

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        "MD",
                        "2050",
                        "A1",
                        null,
                        null
                ),
                result::set
        );

        assertEquals(
                UpdateWarehouseItemResult.Status.SUCCESS,
                result.get().getStatus()
        );

        assertEquals(
                "MD",
                repository.updatedItem.getCategory()
        );

        assertEquals(
                "2050",
                repository.updatedItem.getCode()
        );
    }

    @Test
    public void roomDuplicateRemainsFinalProtection() {
        FakeRepository repository = new FakeRepository();

        repository.existingItem = createExistingItem();
        repository.duplicateExists = false;
        repository.updateOutcome =
                FakeRepository.UpdateOutcome.DUPLICATE;

        UpdateWarehouseItemService service =
                createService(repository);

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        "MD",
                        "1050",
                        "A1",
                        null,
                        null
                ),
                result::set
        );

        assertTrue(repository.duplicateCheckCalled);
        assertTrue(repository.updatedItem != null);

        assertEquals(
                UpdateWarehouseItemResult.Status.DUPLICATE,
                result.get().getStatus()
        );
    }

    @Test
    public void duplicateCheckErrorReturnsPersistenceError() {
        FakeRepository repository = new FakeRepository();

        repository.existingItem = createExistingItem();

        repository.duplicateCheckError =
                new IllegalStateException(
                        "Duplicate check failed"
                );

        UpdateWarehouseItemService service =
                createService(repository);

        AtomicReference<UpdateWarehouseItemResult> result =
                new AtomicReference<>();

        service.updateWarehouseItem(
                new UpdateWarehouseItemCommand(
                        7L,
                        "MR",
                        "1050",
                        "A1",
                        null,
                        null
                ),
                result::set
        );

        assertNull(repository.updatedItem);

        assertEquals(
                UpdateWarehouseItemResult.Status
                        .PERSISTENCE_ERROR,
                result.get().getStatus()
        );
    }

    private UpdateWarehouseItemService createService(
            FakeRepository repository
    ) {
        return new UpdateWarehouseItemService(
                repository,
                () -> NEW_UPDATED_AT
        );
    }

    private WarehouseItem createExistingItem() {
        return new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                "Nivel 1",
                null,
                ORIGINAL_CREATED_AT,
                200L
        );
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        private enum UpdateOutcome {
            SUCCESS,
            DUPLICATE,
            NOT_FOUND,
            ERROR
        }

        private WarehouseItem existingItem;
        private WarehouseItem updatedItem;
        private boolean findCalled;

        private UpdateOutcome updateOutcome =
                UpdateOutcome.SUCCESS;

        private boolean duplicateExists;
        private boolean duplicateCheckCalled;
        private String checkedCategory;
        private String checkedCode;
        private long excludedWarehouseItemId;
        private Throwable duplicateCheckError;

        @Override
        public void findById(
                long warehouseItemId,
                WarehouseItemFindCallback callback
        ) {
            findCalled = true;

            if (existingItem == null) {
                callback.onNotFound();
                return;
            }

            callback.onFound(existingItem);
        }

        @Override
        public void update(
                WarehouseItem warehouseItem,
                WarehouseItemUpdateCallback callback
        ) {
            updatedItem = warehouseItem;

            switch (updateOutcome) {
                case DUPLICATE:
                    callback.onDuplicate();
                    break;

                case NOT_FOUND:
                    callback.onNotFound();
                    break;

                case ERROR:
                    callback.onError(
                            new IllegalStateException(
                                    "Unexpected error"
                            )
                    );
                    break;

                case SUCCESS:
                default:
                    callback.onSuccess();
                    break;
            }
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
        public void insert(
                WarehouseItem warehouseItem,
                WarehouseItemInsertCallback callback
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
            duplicateCheckCalled = true;
            checkedCategory = category;
            checkedCode = code;
            this.excludedWarehouseItemId =
                    excludedWarehouseItemId;

            if (duplicateCheckError != null) {
                callback.onError(duplicateCheckError);
                return;
            }

            callback.onResult(duplicateExists);
        }
    }
}
