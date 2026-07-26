package com.rndymi.almacentracker.core.document;

public interface DocumentRecognitionCallback {

    void onSuccess(RecognizedDocument document);

    void onImageOpenError();

    void onRecognitionError();
}