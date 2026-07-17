package com.rndymi.almacentracker.application.port.in;

import androidx.lifecycle.LiveData;

import com.rndymi.almacentracker.application.result.WarehouseItemDetailResult;
public interface GetWarehouseItemDetailUseCase {
    LiveData<WarehouseItemDetailResult> observerWarehouseItemDetail(
            long warehouseItemId
    );
}
