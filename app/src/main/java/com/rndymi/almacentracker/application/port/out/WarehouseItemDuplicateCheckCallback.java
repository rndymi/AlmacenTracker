package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemDuplicateCheckCallback {

    void onResult(boolean exists);

    void onError(Throwable throwable);
}
