package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemDeleteCallback {

    void onSuccess();

    void onNotFound();

    void onError(Throwable throwable);
}