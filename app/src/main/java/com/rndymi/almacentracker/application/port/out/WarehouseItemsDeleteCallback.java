package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemsDeleteCallback {

    void onComplete(int deletedCount);

    void onError(Throwable throwable);
}