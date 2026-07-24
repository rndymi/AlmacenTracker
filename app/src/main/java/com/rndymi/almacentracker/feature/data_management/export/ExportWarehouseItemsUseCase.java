package com.rndymi.almacentracker.feature.data_management.export;

public interface ExportWarehouseItemsUseCase {

    interface Callback {
        void onResult(ExportWarehouseItemsResult result);
    }

    void exportWarehouseItems(
            String destinationReference,
            Callback callback
    );
}