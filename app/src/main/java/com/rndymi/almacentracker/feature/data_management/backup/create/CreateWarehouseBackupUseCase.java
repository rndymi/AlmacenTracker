package com.rndymi.almacentracker.feature.data_management.backup.create;

public interface CreateWarehouseBackupUseCase {

    interface Callback {
        void onResult(CreateWarehouseBackupResult result);
    }

    void createBackup(
            String destinationReference,
            Callback callback
    );
}