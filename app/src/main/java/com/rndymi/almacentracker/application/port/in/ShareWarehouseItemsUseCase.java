package com.rndymi.almacentracker.application.port.in;

import com.rndymi.almacentracker.application.result.ShareWarehouseItemsResult;

public interface ShareWarehouseItemsUseCase {

    void prepareWarehouseItemsForSharing(Callback callback);

    interface Callback {
        void onResult(ShareWarehouseItemsResult result);
    }
}