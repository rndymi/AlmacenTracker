package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

public interface WarehouseItemFindCallback {
    void onFound(WarehouseItem warehouseItem);
    void onNotFound();
    void onError(Throwable throwable);
}