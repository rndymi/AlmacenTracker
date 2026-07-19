package com.rndymi.almacentracker.application.port.out;

public interface WarehouseBackupCsvReader {

    void readBackup(
            String sourceReference,
            WarehouseBackupCsvReadCallback callback
    );
}