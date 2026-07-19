package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemsInsertCallback {

    void onSuccess(int insertedCount);

    void onDuplicate(Throwable throwable);

    void onError(Throwable throwable);
}