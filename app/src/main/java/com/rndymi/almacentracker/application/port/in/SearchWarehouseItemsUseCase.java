package com.rndymi.almacentracker.application.port.in;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

public interface SearchWarehouseItemsUseCase {
    LiveData<WarehouseItemsResult> search(String query);
}
