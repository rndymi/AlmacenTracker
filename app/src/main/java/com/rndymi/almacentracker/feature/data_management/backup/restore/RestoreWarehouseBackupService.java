package com.rndymi.almacentracker.feature.data_management.backup.restore;

import com.rndymi.almacentracker.data.repository.RepositoryCallback;
import com.rndymi.almacentracker.data.repository.WarehouseItemRepository;
import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class RestoreWarehouseBackupService {

    private final WarehouseItemRepository repository;

    public RestoreWarehouseBackupService(
            WarehouseItemRepository repository
    ) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void restoreBackup(
            List<WarehouseItem> warehouseItems,
            Consumer<RestoreWarehouseBackupResult> callback
    ) {
        Objects.requireNonNull(callback);

        if (warehouseItems == null) {
            callback.accept(
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
                new RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(
                            Integer replacedCount
                    ) {
                        if (replacedCount
                                != snapshot.size()) {
                            callback.accept(
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

                        callback.accept(
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
                        callback.accept(
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
                        callback.accept(
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
