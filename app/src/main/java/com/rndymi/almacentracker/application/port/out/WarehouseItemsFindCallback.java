package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseItemsFindCallback {
    void onSuccess(List<WarehouseItem> warehouseItems);
    void onError(Throwable throwable);
}