package com.rndymi.almacentracker.core.csv.exchange;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseItemCsvExporter {

    void export(
            String destinationReference,
            List<WarehouseItem> warehouseItems,
            ExportCallback callback
    );

    interface ExportCallback {

        void onSuccess();

        void onInvalidDestination();

        void onSerializationError(Throwable throwable);

        void onWriteError(Throwable throwable);

        void onUnknownError(Throwable throwable);
    }
}
