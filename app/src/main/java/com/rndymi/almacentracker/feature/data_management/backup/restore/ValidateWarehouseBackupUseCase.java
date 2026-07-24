package com.rndymi.almacentracker.feature.data_management.backup.restore;

import com.rndymi.almacentracker.application.result.WarehouseBackupValidationResult;

public interface ValidateWarehouseBackupUseCase {

    void validateBackup(
            String sourceReference,
            Callback callback
    );

    @FunctionalInterface
    interface Callback {
        void onResult(
                WarehouseBackupValidationResult result
        );
    }
}