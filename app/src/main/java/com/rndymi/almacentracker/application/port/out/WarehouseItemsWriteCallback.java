package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemsWriteCallback {

    void onSuccess(int writtenCount);

    void onDuplicate(Throwable cause);

    void onError(Throwable cause);
}
