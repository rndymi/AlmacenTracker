package com.rndymi.almacentracker.application.service;

import com.rndymi.almacentracker.application.port.in.RestoreWarehouseBackupUseCase;
import com.rndymi.almacentracker.application.port.out.WarehouseItemRepository;
import com.rndymi.almacentracker.application.port.out.WarehouseItemsReplaceCallback;
import com.rndymi.almacentracker.application.result.RestoreWarehouseBackupResult;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class RestoreWarehouseBackupService
        implements RestoreWarehouseBackupUseCase {

    private final WarehouseItemRepository repository;

    public RestoreWarehouseBackupService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public void restoreBackup(
            List<WarehouseItem> warehouseItems,
            Callback callback
    ) {
        Objects.requireNonNull(callback);

        if (warehouseItems == null) {
            callback.onResult(
                    RestoreWarehouseBackupResult.failure(
                            RestoreWarehouseBackupResult
                                    .Status.INVALID_BACKUP,
                            new IllegalArgumentException(
                                    "Validated backup cannot be null"
                            )
                    )
            );
            return;
        }

        List<WarehouseItem> snapshot =
                Collections.unmodifiableList(
                        new ArrayList<>(warehouseItems)
                );

        repository.replaceAll(
                snapshot,
                new WarehouseItemsReplaceCallback() {
                    @Override
                    public void onSuccess(
                            int replacedCount
                    ) {
                        if (replacedCount
                                != snapshot.size()) {
                            callback.onResult(
                                    RestoreWarehouseBackupResult
                                            .failure(
                                                    RestoreWarehouseBackupResult
                                                            .Status
                                                            .PERSISTENCE_ERROR,
                                                    new IllegalStateException(
                                                            "Unexpected restored count"
                                                    )
                                            )
                            );
                            return;
                        }

                        callback.onResult(
                                RestoreWarehouseBackupResult
                                        .success(
                                                replacedCount
                                        )
                        );
                    }

                    @Override
                    public void onDuplicate(
                            Throwable cause
                    ) {
                        callback.onResult(
                                RestoreWarehouseBackupResult.failure(
                                        RestoreWarehouseBackupResult
                                                .Status.DUPLICATE_DATA,
                                        cause
                                )
                        );
                    }

                    @Override
                    public void onError(
                            Throwable cause
                    ) {
                        callback.onResult(
                                RestoreWarehouseBackupResult.failure(
                                        RestoreWarehouseBackupResult
                                                .Status
                                                .PERSISTENCE_ERROR,
                                        cause
                                )
                        );
                    }
                }
        );
    }
}