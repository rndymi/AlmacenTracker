package com.rndymi.almacentracker.application.port.out;

public interface WarehouseBackupCsvExportCallback {

    void onSuccess();

    void onInvalidDestination();

    void onInvalidData(Throwable throwable);

    void onSerializationError(Throwable throwable);

    void onWriteError(Throwable throwable);

    void onUnknownError(Throwable throwable);
}