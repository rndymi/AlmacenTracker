package com.rndymi.almacentracker.data.document.onnx;

import java.util.Objects;

public final class PaddleOcrModelConfiguration {

    private final String detectorAssetPath;
    private final String recognizerAssetPath;
    private final String dictionaryAssetPath;
    private final String manifestAssetPath;

    public PaddleOcrModelConfiguration(
            String detectorAssetPath,
            String recognizerAssetPath,
            String dictionaryAssetPath,
            String manifestAssetPath
    ) {
        this.detectorAssetPath =
                requireAssetPath(
                        detectorAssetPath,
                        "detectorAssetPath"
                );
        this.recognizerAssetPath =
                requireAssetPath(
                        recognizerAssetPath,
                        "recognizerAssetPath"
                );
        this.dictionaryAssetPath =
                requireAssetPath(
                        dictionaryAssetPath,
                        "dictionaryAssetPath"
                );
        this.manifestAssetPath =
                requireAssetPath(
                        manifestAssetPath,
                        "manifestAssetPath"
                );

        if (this.detectorAssetPath.equals(
                this.recognizerAssetPath
        )) {
            throw new IllegalArgumentException(
                    "Detector and recognizer assets must be different"
            );
        }
    }

    public static PaddleOcrModelConfiguration bundled() {
        String basePath = "ocr/ppocrv5/";

        return new PaddleOcrModelConfiguration(
                basePath + "ppocrv5_mobile_det.onnx",
                basePath + "ppocrv5_mobile_rec.onnx",
                basePath + "ppocrv5_mobile_rec_dict.txt",
                basePath + "model_manifest.properties"
        );
    }

    public String getDetectorAssetPath() {
        return detectorAssetPath;
    }

    public String getRecognizerAssetPath() {
        return recognizerAssetPath;
    }

    public String getDictionaryAssetPath() {
        return dictionaryAssetPath;
    }

    public String getManifestAssetPath() {
        return manifestAssetPath;
    }

    private static String requireAssetPath(
            String value,
            String fieldName
    ) {
        String path = Objects.requireNonNull(
                value,
                fieldName
        ).trim();

        if (path.isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty"
            );
        }

        if (path.startsWith("/")
                || path.contains("..")
                || path.contains("\\")) {
            throw new IllegalArgumentException(
                    fieldName + " is not a valid asset path"
            );
        }

        return path;
    }
}
