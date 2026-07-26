package com.rndymi.almacentracker.core.document;

public interface DocumentTextRecognizer {

    void recognize(
            String imageUri,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    );

    void close();
}