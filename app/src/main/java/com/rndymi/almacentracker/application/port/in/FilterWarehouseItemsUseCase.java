package com.rndymi.almacentracker.application.port.in;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.result.WarehouseItemsResult;

public interface FilterWarehouseItemsUseCase {

    LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    );
}