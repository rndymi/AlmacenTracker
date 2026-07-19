package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseBackupCsvExporter {

    void exportBackup(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            WarehouseBackupCsvExportCallback callback
    );
}