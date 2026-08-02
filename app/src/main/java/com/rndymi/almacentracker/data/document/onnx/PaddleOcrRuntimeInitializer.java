package com.rndymi.almacentracker.data.document.onnx;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;
import java.util.Properties;

public final class PaddleOcrRuntimeInitializer {

    private final OrtEnvironment environment;
    private final OnnxModelAssetLoader assetLoader;
    private final PaddleOcrModelConfiguration configuration;
    private final PaddleOcrSessionMetadataValidator metadataValidator;

    public PaddleOcrRuntimeInitializer(
            OrtEnvironment environment,
            OnnxModelAssetLoader assetLoader,
            PaddleOcrModelConfiguration configuration,
            PaddleOcrSessionMetadataValidator metadataValidator
    ) {
        this.environment =
                Objects.requireNonNull(
                        environment,
                        "environment"
                );
        this.assetLoader =
                Objects.requireNonNull(
                        assetLoader,
                        "assetLoader"
                );
        this.configuration =
                Objects.requireNonNull(
                        configuration,
                        "configuration"
                );
        this.metadataValidator =
                Objects.requireNonNull(
                        metadataValidator,
                        "metadataValidator"
                );
    }

    public PaddleOcrInitializationResult initialize() {
        OrtSession detectorSession = null;
        OrtSession recognizerSession = null;

        try {
            Properties properties =
                    assetLoader.loadProperties(
                            configuration
                                    .getManifestAssetPath()
                    );

            PaddleOcrModelManifest manifest =
                    PaddleOcrModelManifest.from(
                            properties
                    );

            byte[] detectorBytes =
                    assetLoader.loadRequiredBytes(
                            configuration
                                    .getDetectorAssetPath()
                    );
            byte[] recognizerBytes =
                    assetLoader.loadRequiredBytes(
                            configuration
                                    .getRecognizerAssetPath()
                    );
            byte[] dictionaryBytes =
                    assetLoader.loadRequiredBytes(
                            configuration
                                    .getDictionaryAssetPath()
                    );

            assetLoader.verifySha256(
                    detectorBytes,
                    manifest.getDetectorSha256()
            );
            assetLoader.verifySha256(
                    recognizerBytes,
                    manifest.getRecognizerSha256()
            );
            assetLoader.verifySha256(
                    dictionaryBytes,
                    manifest.getDictionarySha256()
            );

            PaddleOcrDictionary dictionary =
                    PaddleOcrDictionary.fromUtf8(
                            dictionaryBytes
                    );

            try (OrtSession.SessionOptions options =
                         createSessionOptions()) {

                detectorSession =
                        environment.createSession(
                                detectorBytes,
                                options
                        );

                metadataValidator.validateDetector(
                        detectorSession,
                        manifest
                );

                recognizerSession =
                        environment.createSession(
                                recognizerBytes,
                                options
                        );

                metadataValidator.validateRecognizer(
                        recognizerSession,
                        manifest,
                        dictionary
                );
            }

            PaddleOcrSessionBundle sessionBundle =
                    new PaddleOcrSessionBundle(
                            detectorSession,
                            recognizerSession,
                            dictionary
                    );

            detectorSession = null;
            recognizerSession = null;

            return PaddleOcrInitializationResult.ready(
                    sessionBundle
            );
        } catch (FileNotFoundException exception) {
            return error(
                    PaddleOcrInitializationError
                            .RESOURCE_NOT_FOUND,
                    exception,
                    recognizerSession,
                    detectorSession
            );
        } catch (IOException exception) {
            return error(
                    PaddleOcrInitializationError
                            .RESOURCE_EMPTY,
                    exception,
                    recognizerSession,
                    detectorSession
            );
        } catch (IllegalArgumentException exception) {
            return error(
                    mapValidationError(exception),
                    exception,
                    recognizerSession,
                    detectorSession
            );
        } catch (OrtException exception) {
            return error(
                    mapOrtError(
                            detectorSession,
                            recognizerSession
                    ),
                    exception,
                    recognizerSession,
                    detectorSession
            );
        } catch (RuntimeException exception) {
            return error(
                    PaddleOcrInitializationError.UNKNOWN,
                    exception,
                    recognizerSession,
                    detectorSession
            );
        }
    }

    private OrtSession.SessionOptions
    createSessionOptions() throws OrtException {
        OrtSession.SessionOptions options =
                new OrtSession.SessionOptions();

        options.setOptimizationLevel(
                OrtSession.SessionOptions
                        .OptLevel.ALL_OPT
        );
        options.setExecutionMode(
                OrtSession.SessionOptions
                        .ExecutionMode.SEQUENTIAL
        );
        options.setIntraOpNumThreads(1);
        options.setInterOpNumThreads(1);

        return options;
    }

    private PaddleOcrInitializationResult error(
            PaddleOcrInitializationError error,
            Throwable cause,
            OrtSession recognizerSession,
            OrtSession detectorSession
    ) {
        closeQuietly(recognizerSession);
        closeQuietly(detectorSession);

        return PaddleOcrInitializationResult.error(
                error,
                cause
        );
    }

    private PaddleOcrInitializationError
    mapValidationError(
            IllegalArgumentException exception
    ) {
        String message = exception.getMessage();

        if (message == null) {
            return PaddleOcrInitializationError.UNKNOWN;
        }

        if (message.contains("SHA-256")
                || message.contains("integrity")) {
            return PaddleOcrInitializationError
                    .RESOURCE_INTEGRITY_ERROR;
        }

        if (message.contains("manifest")
                || message.contains("configured")) {
            return PaddleOcrInitializationError
                    .MANIFEST_INVALID;
        }

        if (message.contains("dictionary")) {
            return PaddleOcrInitializationError
                    .DICTIONARY_INCOMPATIBLE;
        }

        if (message.contains("Recognizer")
                || message.contains("recognizer")) {
            return PaddleOcrInitializationError
                    .RECOGNIZER_METADATA_INCOMPATIBLE;
        }

        if (message.contains("Model")
                || message.contains("model")) {
            return PaddleOcrInitializationError
                    .DETECTOR_METADATA_INCOMPATIBLE;
        }

        return PaddleOcrInitializationError.UNKNOWN;
    }

    private PaddleOcrInitializationError mapOrtError(
            OrtSession detectorSession,
            OrtSession recognizerSession
    ) {
        if (detectorSession == null) {
            return PaddleOcrInitializationError
                    .DETECTOR_SESSION_ERROR;
        }

        if (recognizerSession == null) {
            return PaddleOcrInitializationError
                    .RECOGNIZER_SESSION_ERROR;
        }

        return PaddleOcrInitializationError
                .RUNTIME_UNAVAILABLE;
    }

    private void closeQuietly(
            OrtSession session
    ) {
        if (session == null) {
            return;
        }

        try {
            session.close();
        } catch (RuntimeException ignored) {
        }
    }
}
