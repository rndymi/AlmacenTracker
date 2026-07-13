package com.rndymi.almacentracker.application.port.out;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

public interface WarehouseItemRepository {
    LiveData<WarehouseItemsResult> observeAll();
}
