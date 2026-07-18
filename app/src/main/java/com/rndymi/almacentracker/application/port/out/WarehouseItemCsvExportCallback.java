package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemCsvExportCallback {
    void onSuccess();
    void onInvalidDestination();
    void onSerializationError(Throwable throwable);
    void onWriteError(Throwable throwable);
    void onUnknownError(Throwable throwable);
}