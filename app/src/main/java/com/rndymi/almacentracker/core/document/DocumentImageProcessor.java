package com.rndymi.almacentracker.core.document;

public interface DocumentImageProcessor {
    void process(
            String imageUri,
            DocumentImageProcessingCallback callback
    );
    void close();
}
