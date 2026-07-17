package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;

import java.util.function.Consumer;

public interface DeleteWarehouseItemUseCase {

    void deleteWarehouseItem(
            long warehouseItemId,
            Consumer<DeleteWarehouseItemResult> callback
    );
}