package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.application.result.WarehouseBackupReadResult;

@FunctionalInterface
public interface WarehouseBackupCsvReadCallback {

    void onResult(WarehouseBackupReadResult result);
}