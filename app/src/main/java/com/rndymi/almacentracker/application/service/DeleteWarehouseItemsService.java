package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.DeleteWarehouseItemsUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsDeleteCallback;
import com.rndymi.almacentracker.application.result.DeleteWarehouseItemsResult;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public final class DeleteWarehouseItemsService
        implements DeleteWarehouseItemsUseCase {

    private final WarehouseItemRepository repository;

    public DeleteWarehouseItemsService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void deleteWarehouseItems(
            Set<Long> warehouseItemIds,
            Consumer<DeleteWarehouseItemsResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (warehouseItemIds == null
                || warehouseItemIds.isEmpty()) {
            callback.accept(
                    DeleteWarehouseItemsResult.emptySelection()
            );
            return;
        }

        Set<Long> uniqueIds =
                new LinkedHashSet<>(warehouseItemIds);

        int requestedCount = uniqueIds.size();

        for (Long warehouseItemId : uniqueIds) {
            if (warehouseItemId == null
                    || warehouseItemId <= 0L) {
                callback.accept(
                        DeleteWarehouseItemsResult.invalidIds(
                                requestedCount
                        )
                );
                return;
            }
        }

        List<Long> immutableIds =
                List.copyOf(
                        new ArrayList<>(uniqueIds)
                );

        repository.deleteByIds(
                immutableIds,
                new WarehouseItemsDeleteCallback() {
                    @Override
                    public void onComplete(int deletedCount) {
                        if (deletedCount <= 0) {
                            callback.accept(
                                    DeleteWarehouseItemsResult
                                            .notFound(
                                                    requestedCount
                                            )
                            );
                            return;
                        }

                        if (deletedCount < requestedCount) {
                            callback.accept(
                                    DeleteWarehouseItemsResult
                                            .partialSuccess(
                                                    requestedCount,
                                                    deletedCount
                                            )
                            );
                            return;
                        }

                        callback.accept(
                                DeleteWarehouseItemsResult.success(
                                        requestedCount
                                )
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                DeleteWarehouseItemsResult
                                        .persistenceError(
                                                requestedCount,
                                                throwable
                                        )
                        );
                    }
                }
        );
    }
}