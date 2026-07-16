package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.CreateWarehouseItemResult;

import java.util.function.Consumer;

public interface CreateWarehouseItemUseCase {
    void createWarehouseItem(
            CreateWarehouseItemCommand command,
            Consumer<CreateWarehouseItemResult> callback
    );
}
