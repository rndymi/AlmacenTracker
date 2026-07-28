package com.rndymi.almacentracker.core.document;

public interface DocumentImageProcessingCallback {
    void onSuccess(DocumentImage documentImage);
    void onImageOpenError();
    void onProcessingError();
}
