package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemUpdateCallback {
    void onSuccess();
    void onDuplicate();
    void onNotFound();
    void onError(Throwable throwable);
}