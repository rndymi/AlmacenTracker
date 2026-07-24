package com.rndymi.almacentracker.core.csv.backup;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseBackupCsvExporter {

    void exportBackup(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            ExportCallback callback
    );

    interface ExportCallback {

        void onSuccess();

        void onInvalidDestination();

        void onInvalidData(Throwable throwable);

        void onSerializationError(Throwable throwable);

        void onWriteError(Throwable throwable);

        void onUnknownError(Throwable throwable);
    }
}
