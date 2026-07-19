package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
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
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class CreateWarehouseItemServiceTest {

    private static final long FIXED_TIME = 123456789L;

    @Test
    public void validCommandIsNormalizedAndInserted() {
        FakeRepository repository = new FakeRepository();
        CreateWarehouseItemService service =
                createService(repository);

        AtomicReference<CreateWarehouseItemResult>
                resultReference = new AtomicReference<>();

        service.createWarehouseItem(
                new CreateWarehouseItemCommand(
                        "  mr ",
                        "  ab-1050 ",
                        "  a1 ",
                        " Nivel 2 ",
                        " Caja exterior dañada "
                ),
                resultReference::set
        );

        assertEquals(
                "MR",
                repository.insertedItem.getCategory()
        );

        assertEquals(
                "AB-1050",
                repository.insertedItem.getCode()
        );

        assertEquals(
                "A1",
                repository.insertedItem.getSite()
        );

        assertEquals(
                "Nivel 2",
                repository.insertedItem.getPosition()
        );

        assertEquals(
                "Caja exterior dañada",
                repository.insertedItem.getObservations()
        );

        assertEquals(
                FIXED_TIME,
                repository.insertedItem.getCreatedAt()
        );

        assertEquals(
                FIXED_TIME,
                repository.insertedItem.getUpdatedAt()
        );

        assertEquals(
                CreateWarehouseItemResult.Status.SUCCESS,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void emptyOptionalFieldsBecomeNull() {
        FakeRepository repository = new FakeRepository();
        CreateWarehouseItemService service =
                createService(repository);

        service.createWarehouseItem(
                new CreateWarehouseItemCommand(
                        "MR",
                        "1050",
                        "A1",
                        "   ",
                        ""
                ),
                ignored -> {
                }
        );

        assertNull(repository.insertedItem.getPosition());
        assertNull(
                repository.insertedItem.getObservations()
        );
    }

    @Test
    public void requiredFieldsAreValidatedBeforeRepository() {
        FakeRepository repository = new FakeRepository();
        CreateWarehouseItemService service =
                createService(repository);

        AtomicReference<CreateWarehouseItemResult>
                resultReference = new AtomicReference<>();

        service.createWarehouseItem(
                new CreateWarehouseItemCommand(
                        " ",
                        "",
                        "   ",
                        null,
                        null
                ),
                resultReference::set
        );

        CreateWarehouseItemResult result =
                resultReference.get();

        assertEquals(
                CreateWarehouseItemResult.Status
                        .VALIDATION_ERROR,
                result.getStatus()
        );

        assertTrue(result.isCategoryRequired());
        assertTrue(result.isCodeRequired());
        assertTrue(result.isSiteRequired());
        assertFalse(repository.insertCalled);
        assertFalse(repository.duplicateCheckCalled);
    }

    @Test
    public void roomDuplicateIsStillReturnedAsApplicationResult() {
        FakeRepository repository = new FakeRepository();
        repository.duplicate = true;

        CreateWarehouseItemService service =
                createService(repository);

        AtomicReference<CreateWarehouseItemResult>
                resultReference = new AtomicReference<>();

        service.createWarehouseItem(
                validCommand(),
                resultReference::set
        );

        assertTrue(repository.duplicateCheckCalled);
        assertTrue(repository.insertCalled);

        assertEquals(
                CreateWarehouseItemResult.Status.DUPLICATE,
                resultReference.get().getStatus()
        );
    }


    @Test
    public void duplicateCheckUsesNormalizedCategoryAndCode() {
        FakeRepository repository = new FakeRepository();

        CreateWarehouseItemService service =
                createService(repository);

        service.createWarehouseItem(
                new CreateWarehouseItemCommand(
                        "  mr ",
                        " ab-1050 ",
                        "A1",
                        null,
                        null
                ),
                ignored -> {
                }
        );

        assertTrue(repository.duplicateCheckCalled);
        assertEquals("MR", repository.checkedCategory);
        assertEquals("AB-1050", repository.checkedCode);
    }

    @Test
    public void existingCombinationReturnsDuplicateWithoutInsert() {
        FakeRepository repository = new FakeRepository();
        repository.duplicateExists = true;

        CreateWarehouseItemService service =
                createService(repository);

        AtomicReference<CreateWarehouseItemResult>
                resultReference = new AtomicReference<>();

        service.createWarehouseItem(
                validCommand(),
                resultReference::set
        );

        assertTrue(repository.duplicateCheckCalled);
        assertFalse(repository.insertCalled);

        assertEquals(
                CreateWarehouseItemResult.Status.DUPLICATE,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void availableCombinationIsInserted() {
        FakeRepository repository = new FakeRepository();
        repository.duplicateExists = false;

        CreateWarehouseItemService service =
                createService(repository);

        AtomicReference<CreateWarehouseItemResult>
                resultReference = new AtomicReference<>();

        service.createWarehouseItem(
                validCommand(),
                resultReference::set
        );

        assertTrue(repository.duplicateCheckCalled);
        assertTrue(repository.insertCalled);

        assertEquals(
                CreateWarehouseItemResult.Status.SUCCESS,
                resultReference.get().getStatus()
        );
    }

    @Test
    public void validationFailureDoesNotCheckDuplicates() {
        FakeRepository repository = new FakeRepository();

        CreateWarehouseItemService service =
                createService(repository);

        service.createWarehouseItem(
                new CreateWarehouseItemCommand(
                        " ",
                        " ",
                        " ",
                        null,
                        null
                ),
                ignored -> {
                }
        );

        assertFalse(repository.duplicateCheckCalled);
        assertFalse(repository.insertCalled);
    }

    @Test
    public void duplicateCheckErrorReturnsPersistenceError() {
        FakeRepository repository = new FakeRepository();

        repository.duplicateCheckError =
                new IllegalStateException(
                        "Duplicate check failed"
                );

        CreateWarehouseItemService service =
                createService(repository);

        AtomicReference<CreateWarehouseItemResult>
                resultReference = new AtomicReference<>();

        service.createWarehouseItem(
                validCommand(),
                resultReference::set
        );

        assertFalse(repository.insertCalled);

        assertEquals(
                CreateWarehouseItemResult.Status
                        .PERSISTENCE_ERROR,
                resultReference.get().getStatus()
        );
    }

    private CreateWarehouseItemService createService(
            FakeRepository repository
    ) {
        return new CreateWarehouseItemService(
                repository,
                () -> FIXED_TIME
        );
    }

    private CreateWarehouseItemCommand validCommand() {
        return new CreateWarehouseItemCommand(
                "MR",
                "1050",
                "A1",
                null,
                null
        );
    }

    private static final class FakeRepository
            implements WarehouseItemRepository {

        private WarehouseItem insertedItem;
        private boolean insertCalled;
        private boolean duplicate;

        private boolean duplicateExists;
        private boolean duplicateCheckCalled;
        private String checkedCategory;
        private String checkedCode;
        private Throwable duplicateCheckError;

        @Override
        public LiveData<WarehouseItemsResult> observeAll() {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemDetailResult> observeById(
                long warehouseItemId
        ) {
            return new MutableLiveData<>(
                    WarehouseItemDetailResult.notFound());
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
            insertCalled = true;
            insertedItem = warehouseItem;

            if (duplicate) {
                callback.onDuplicate();
            } else {
                callback.onSuccess(10L);
            }
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

        @Override
        public void deleteByIds(
                List<Long> warehouseItemIds,
                WarehouseItemsDeleteCallback callback
        ) {
            throw new UnsupportedOperationException();
        }

        @Override
        public LiveData<WarehouseItemsResult> search(
                String query
        ) {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemsResult> filter(
                WarehouseItemFilterCriteria criteria
        ) {
            return new MutableLiveData<>();
        }

        @Override
        public LiveData<WarehouseItemFilterOptionsResult>
        observeFilterOptions() {
            return new MutableLiveData<>();
        }

        @Override
        public void existsByCategoryAndCode(
                String category,
                String code,
                WarehouseItemDuplicateCheckCallback callback
        ) {
            duplicateCheckCalled = true;
            checkedCategory = category;
            checkedCode = code;

            if (duplicateCheckError != null) {
                callback.onError(duplicateCheckError);
                return;
            }

            callback.onResult(duplicateExists);
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
