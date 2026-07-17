package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemDeleteCallback;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemResult;

import java.util.Objects;
import java.util.function.Consumer;

public final class DeleteWarehouseItemService
        implements DeleteWarehouseItemUseCase {

    private final WarehouseItemRepository repository;

    public DeleteWarehouseItemService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void deleteWarehouseItem(
            long warehouseItemId,
            Consumer<DeleteWarehouseItemResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (warehouseItemId <= 0L) {
            callback.accept(
                    DeleteWarehouseItemResult.invalidId()
            );
            return;
        }

        repository.deleteById(
                warehouseItemId,
                new WarehouseItemDeleteCallback() {
                    @Override
                    public void onSuccess() {
                        callback.accept(
                                DeleteWarehouseItemResult.success()
                        );
                    }

                    @Override
                    public void onNotFound() {
                        callback.accept(
                                DeleteWarehouseItemResult.notFound()
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                DeleteWarehouseItemResult
                                        .persistenceError(
                                                throwable
                                        )
                        );
                    }
                }
        );
    }
}