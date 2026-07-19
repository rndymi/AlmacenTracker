package com.rndymi.almacentracker.application.port.out;

public interface WarehouseItemCsvReader {

    void read(
            String sourceReference,
            WarehouseItemCsvReadCallback callback
    );
}