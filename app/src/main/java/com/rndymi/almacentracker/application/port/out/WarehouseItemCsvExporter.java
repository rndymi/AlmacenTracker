package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseItemCsvExporter {
    void export(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            WarehouseItemCsvExportCallback callback
    );
}