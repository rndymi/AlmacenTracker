package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.application.result.WarehouseItemCsvReadResult;

public interface WarehouseItemCsvReadCallback {

    void onSuccess(WarehouseItemCsvReadResult result);

    void onInvalidFormat();

    void onReadError(Throwable throwable);

    void onUnknownError(Throwable throwable);
}