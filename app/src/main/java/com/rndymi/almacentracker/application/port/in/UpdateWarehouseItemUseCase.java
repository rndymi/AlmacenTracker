package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.UpdateWarehouseItemResult;

import java.util.function.Consumer;

public interface UpdateWarehouseItemUseCase {

    void updateWarehouseItem(
            UpdateWarehouseItemCommand command,
            Consumer<UpdateWarehouseItemResult> callback
    );
}