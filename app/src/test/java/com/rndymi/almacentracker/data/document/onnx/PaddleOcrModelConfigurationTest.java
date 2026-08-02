package com.rndymi.almacentracker.data.document.onnx;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public final class PaddleOcrModelConfigurationTest {

    @Test
    public void constructor_keepsValidAssetPaths() {
        PaddleOcrModelConfiguration configuration =
                new PaddleOcrModelConfiguration(
                        "ocr/detector.onnx",
                        "ocr/recognizer.onnx",
                        "ocr/dictionary.txt",
                        "ocr/manifest.properties"
                );

        assertEquals(
                "ocr/detector.onnx",
                configuration.getDetectorAssetPath()
        );
        assertEquals(
                "ocr/recognizer.onnx",
                configuration.getRecognizerAssetPath()
        );
        assertEquals(
                "ocr/dictionary.txt",
                configuration.getDictionaryAssetPath()
        );
        assertEquals(
                "ocr/manifest.properties",
                configuration.getManifestAssetPath()
        );
    }

    @Test
    public void constructor_rejectsEmptyAssetPath() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaddleOcrModelConfiguration(
                        "",
                        "ocr/recognizer.onnx",
                        "ocr/dictionary.txt",
                        "ocr/manifest.properties"
                )
        );
    }

    @Test
    public void constructor_rejectsParentTraversal() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaddleOcrModelConfiguration(
                        "../detector.onnx",
                        "ocr/recognizer.onnx",
                        "ocr/dictionary.txt",
                        "ocr/manifest.properties"
                )
        );
    }

    @Test
    public void constructor_rejectsSameModelPath() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PaddleOcrModelConfiguration(
                        "ocr/model.onnx",
                        "ocr/model.onnx",
                        "ocr/dictionary.txt",
                        "ocr/manifest.properties"
                )
        );
    }
}
