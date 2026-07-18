package com.rndymi.almacentracker.application.port.out;

import com.rndymi.almacentracker.application.result.ShareableCsvFile;

public interface WarehouseItemCsvShareFileCallback {

    void onSuccess(ShareableCsvFile shareableFile);

    void onSerializationError(Throwable throwable);

    void onTemporaryFileError(Throwable throwable);

    void onFileProviderError(Throwable throwable);

    void onUnknownError(Throwable throwable);
}
