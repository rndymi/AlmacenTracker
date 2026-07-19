package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.CreateWarehouseBackupResult;

public interface CreateWarehouseBackupUseCase {

    interface Callback {
        void onResult(CreateWarehouseBackupResult result);
    }

    void createBackup(
            String destinationReference,
            Callback callback
    );
}