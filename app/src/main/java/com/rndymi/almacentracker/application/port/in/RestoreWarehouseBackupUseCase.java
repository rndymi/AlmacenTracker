package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.RestoreWarehouseBackupResult;
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