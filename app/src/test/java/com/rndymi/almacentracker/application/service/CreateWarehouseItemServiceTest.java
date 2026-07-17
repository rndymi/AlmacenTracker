package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.in.CreateWarehouseItemCommand;
import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

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
    }

    @Test
    public void duplicateIsReturnedAsApplicationResult() {
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

        assertEquals(
                CreateWarehouseItemResult.Status.DUPLICATE,
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
    }
}
