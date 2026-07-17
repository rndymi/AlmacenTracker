package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.out.WarehouseItemInsertCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import org.junit.Test;

public class ObserveWarehouseItemsServiceTest {
    @Test
    public void observeWarehouseItemsDelegatesToRepository() {
        MutableLiveData<WarehouseItemsResult> expected =
                new MutableLiveData<>();

        WarehouseItemRepository repository =
                new WarehouseItemRepository() {
                    @Override
                    public LiveData<WarehouseItemsResult>
                    observeAll() {
                        return expected;
                    }

                    @Override
                    public LiveData<WarehouseItemsResult> search(
                            String query
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public LiveData<WarehouseItemDetailResult>
                    observeById(long warehouseItemId) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }

                    @Override
                    public void insert(
                            WarehouseItem warehouseItem,
                            WarehouseItemInsertCallback callback
                    ) {
                        throw new UnsupportedOperationException(
                                "Not required by this test"
                        );
                    }
                };

        ObserveWarehouseItemsService service =
                new ObserveWarehouseItemsService(repository);

        LiveData<WarehouseItemsResult> actual =
                service.observeWarehouseItems();

        assertSame(expected, actual);
    }
}
