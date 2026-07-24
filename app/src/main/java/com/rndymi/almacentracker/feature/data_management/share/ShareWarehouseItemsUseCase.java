package com.rndymi.almacentracker.feature.data_management.share;

public interface ShareWarehouseItemsUseCase {

    void prepareWarehouseItemsForSharing(Callback callback);

    interface Callback {
        void onResult(ShareWarehouseItemsResult result);
    }
}