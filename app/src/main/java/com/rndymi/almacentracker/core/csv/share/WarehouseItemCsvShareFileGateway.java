package com.rndymi.almacentracker.core.csv.share;

import com.rndymi.almacentracker.domain.model.WarehouseItem;

import java.util.List;

public interface WarehouseItemCsvShareFileGateway {

    void createShareableFile(
            List<WarehouseItem> warehouseItems,
            String suggestedFileName,
            ShareFileCallback callback
    );

    interface ShareFileCallback {

        void onSuccess(ShareableCsvFile shareableFile);

        void onSerializationError(Throwable throwable);

        void onTemporaryFileError(Throwable throwable);

        void onFileProviderError(Throwable throwable);

        void onUnknownError(Throwable throwable);
    }
}
