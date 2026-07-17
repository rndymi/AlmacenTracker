package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.UpdateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.port.out.WarehouseItemFindCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemUpdateCallback;
import com.rndymi.almacentracker.application.result.UpdateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

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

        assertNull(repository.updatedItem);
    }

    @Test
    public void duplicateIsTransformedToApplicationResult() {
        FakeRepository repository = new FakeRepository();

        repository.existingItem = new WarehouseItem(
                7L,
                "MR",
                "1050",
                "A1",
                null,
                null,
                ORIGINAL_CREATED_AT,
                200L
        );

        repository.updateOutcome =
                FakeRepository.UpdateOutcome.DUPLICATE;

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
                        "MD",
                        "1050",
                        "A1",
                        null,
                        null
                ),
                result::set
        );

        assertEquals(
                UpdateWarehouseItemResult.Status.DUPLICATE,
                result.get().getStatus()
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
        public void insert(
                WarehouseItem warehouseItem,
                WarehouseItemInsertCallback callback
        ) {
            throw new UnsupportedOperationException();
        }
    }
}