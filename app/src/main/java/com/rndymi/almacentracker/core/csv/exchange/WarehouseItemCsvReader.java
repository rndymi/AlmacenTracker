package com.rndymi.almacentracker.core.csv.exchange;

public interface WarehouseItemCsvReader {

    void read(
            String sourceReference,
            ReadCallback callback
    );

    interface ReadCallback {

        void onSuccess(WarehouseItemCsvReadResult result);

        void onInvalidFormat();

        void onReadError(Throwable throwable);

        void onUnknownError(Throwable throwable);
    }
}
