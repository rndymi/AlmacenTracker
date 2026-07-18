package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.ExportWarehouseItemsResult;

public interface ExportWarehouseItemsUseCase {

    interface Callback {
        void onResult(ExportWarehouseItemsResult result);
    }

    void exportWarehouseItems(
            String destinationReference,
            Callback callback
    );
}