package com.rndymi.almacentracker.data.document.onnx;

@FunctionalInterface
public interface PaddleOcrRuntimeFactory {

    PaddleOcrInitializationResult initialize();
}
