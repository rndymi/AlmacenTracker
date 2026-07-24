package com.rndymi.almacentracker.feature.data_management.import_data;

public interface ImportWarehouseItemsUseCase {

    void importWarehouseItems(
            String sourceReference,
            Callback callback
    );

    interface Callback {
        void onResult(ImportWarehouseItemsResult result);
    }
}