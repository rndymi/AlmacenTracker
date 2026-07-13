package com.rndymi.almacentracker.application.service;

import static org.junit.Assert.assertSame;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

import org.junit.Test;

public class ObserveWarehouseItemsServiceTest {
    @Test
    public void observeWarehouseItemsDelegatesToRepository() {
        MutableLiveData<WarehouseItemsResult> expected =
                new MutableLiveData<>();

        WarehouseItemRepository repository = () -> expected;

        ObserveWarehouseItemsService service =
                new ObserveWarehouseItemsService(repository);

        LiveData<WarehouseItemsResult> actual =
                service.observeWarehouseItems();

        assertSame(expected, actual);
    }
}