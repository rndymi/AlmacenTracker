package com.rndymi.almacentracker.application.port.out;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.port.in.WarehouseItemFilterCriteria;
import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
import com.rndymi.almacentracker.application.result.WarehouseItemFilterOptionsResult;
import com.rndymi.almacentracker.application.result.WarehouseItemsResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

public interface WarehouseItemRepository {
    LiveData<WarehouseItemsResult> observeAll();
    LiveData<WarehouseItemsResult> search(String query);
    LiveData<WarehouseItemsResult> filter(
            WarehouseItemFilterCriteria criteria
    );
    LiveData<WarehouseItemFilterOptionsResult>
    observeFilterOptions();
    LiveData<WarehouseItemDetailResult> observeById(
            long warehouseItemId
    );
    void insert(
            WarehouseItem warehouseItem,
            WarehouseItemInsertCallback callback
    );
}
