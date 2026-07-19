package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemsReplaceCallback {

    void onSuccess(int replacedCount);

    void onDuplicate(Throwable cause);

    void onError(Throwable cause);
}