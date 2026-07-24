package com.rndymi.almacentracker.feature.data_management.backup.restore;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface RestoreWarehouseBackupUseCase {

    void restoreBackup(
            List<WarehouseItem> warehouseItems,
            Callback callback
    );

    @FunctionalInterface
    interface Callback {
        void onResult(
                RestoreWarehouseBackupResult result
        );
    }
}