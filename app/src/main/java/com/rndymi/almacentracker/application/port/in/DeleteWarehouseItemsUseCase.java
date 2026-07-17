package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;

import java.util.Set;
import java.util.function.Consumer;

public interface DeleteWarehouseItemsUseCase {

    void deleteWarehouseItems(
            Set<Long> warehouseItemIds,
            Consumer<DeleteWarehouseItemsResult> callback
    );
}