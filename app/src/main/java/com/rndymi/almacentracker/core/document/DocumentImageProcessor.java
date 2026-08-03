package com.rndymi.almacentracker.core.document;

public interface DocumentImageProcessor {

    void process(
            DocumentImageProcessingRequest request,
            DocumentImageProcessingCallback callback
    );

    void close();
}
