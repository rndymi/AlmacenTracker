package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemInsertCallback {
    void onSuccess(long createItemId);
    void onDuplicate();
    void onError(Throwable throwable);
}
