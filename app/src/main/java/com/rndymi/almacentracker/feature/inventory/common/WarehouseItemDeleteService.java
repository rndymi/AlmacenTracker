package com.rndymi.almacentracker.feature.inventory.common;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

public class WarehouseItemDeleteService {

    private final WarehouseItemRepository repository;

    public WarehouseItemDeleteService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void delete(
            long warehouseItemId,
            Consumer<WarehouseItemDeleteResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (warehouseItemId <= 0L) {
            callback.accept(
                    WarehouseItemDeleteResult.invalidId(1)
            );
            return;
        }

        repository.deleteById(
                warehouseItemId,
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        callback.accept(
                                WarehouseItemDeleteResult.success(1)
                        );
                    }

                    @Override
                    public void onNotFound() {
                        callback.accept(
                                WarehouseItemDeleteResult.notFound(1)
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemDeleteResult
                                        .persistenceError(
                                                1,
                                                throwable
                                        )
                        );
                    }
                }
        );
    }

    public void delete(
            Set<Long> warehouseItemIds,
            Consumer<WarehouseItemDeleteResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (warehouseItemIds == null
                || warehouseItemIds.isEmpty()) {
            callback.accept(
                    WarehouseItemDeleteResult.emptySelection()
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
                        WarehouseItemDeleteResult.invalidId(
                                requestedCount
                        )
                );
                return;
            }
        }

        List<Long> immutableIds = List.copyOf(
                new ArrayList<>(uniqueIds)
        );

        repository.deleteByIds(
                immutableIds,
                new RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer deletedCount) {
                        if (deletedCount <= 0) {
                            callback.accept(
                                    WarehouseItemDeleteResult
                                            .notFound(
                                                    requestedCount
                                            )
                            );
                            return;
                        }

                        if (deletedCount < requestedCount) {
                            callback.accept(
                                    WarehouseItemDeleteResult
                                            .partialSuccess(
                                                    requestedCount,
                                                    deletedCount
                                            )
                            );
                            return;
                        }

                        callback.accept(
                                WarehouseItemDeleteResult.success(
                                        requestedCount
                                )
                        );
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        callback.accept(
                                WarehouseItemDeleteResult
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
