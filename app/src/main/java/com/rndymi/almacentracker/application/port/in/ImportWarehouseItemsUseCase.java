package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.ImportWarehouseItemsResult;

public interface ImportWarehouseItemsUseCase {

    void importWarehouseItems(
            String sourceReference,
            Callback callback
    );

    interface Callback {
        void onResult(ImportWarehouseItemsResult result);
    }
}