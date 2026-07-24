package com.rndymi.almacentracker.core.csv.backup;

public interface WarehouseBackupCsvReader {

    void readBackup(
            String sourceReference,
            ReadCallback callback
    );

    @FunctionalInterface
    interface ReadCallback {

        void onResult(WarehouseBackupReadResult result);
    }
}
