package com.rndymi.almacentracker.core.document;

public interface DocumentTextRecognizer {
    void recognize(
            DocumentImage documentImage,
            DocumentImageSource sourceType,
            DocumentRecognitionCallback callback
    );
    void close();
}
