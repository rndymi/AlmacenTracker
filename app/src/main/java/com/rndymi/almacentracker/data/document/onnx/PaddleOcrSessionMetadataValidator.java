package com.rndymi.almacentracker.data.document.onnx;

import ai.onnxruntime.NodeInfo;
import ai.onnxruntime.OnnxJavaType;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import ai.onnxruntime.TensorInfo;

import java.util.Map;
import java.util.Objects;

public final class PaddleOcrSessionMetadataValidator {

    public void validateDetector(
            OrtSession session,
            PaddleOcrModelManifest manifest
    ) throws OrtException {
        validateSession(
                session,
                manifest.getDetectorInputName(),
                manifest.getDetectorInputRank(),
                manifest.getDetectorOutputCount(),
                PaddleOcrInitializationError
                        .DETECTOR_METADATA_INCOMPATIBLE
        );
    }

    public void validateRecognizer(
            OrtSession session,
            PaddleOcrModelManifest manifest,
            PaddleOcrDictionary dictionary
    ) throws OrtException {
        validateSession(
                session,
                manifest.getRecognizerInputName(),
                manifest.getRecognizerInputRank(),
                manifest.getRecognizerOutputCount(),
                PaddleOcrInitializationError
                        .RECOGNIZER_METADATA_INCOMPATIBLE
        );

        int expectedDictionarySize =
                manifest.expectedDictionarySize();

        if (expectedDictionarySize <= 0) {
            throw new PaddleOcrManifestException(
                    "Recognizer token metadata is invalid"
            );
        }

        if (dictionary.size()
                != expectedDictionarySize) {
            throw new PaddleOcrMetadataException(
                    PaddleOcrInitializationError
                            .DICTIONARY_INCOMPATIBLE,
                    "Recognition dictionary is incompatible"
            );
        }

        validateRecognizerOutputClassCount(
                session,
                manifest.getRecognizerClassCount()
        );
    }

    private void validateSession(
            OrtSession session,
            String expectedInputName,
            int expectedInputRank,
            int expectedOutputCount,
            PaddleOcrInitializationError error
    ) throws OrtException {
        Objects.requireNonNull(
                session,
                "session"
        );

        Map<String, NodeInfo> inputInfo =
                session.getInputInfo();

        NodeInfo inputNode =
                inputInfo.get(expectedInputName);

        if (inputNode == null) {
            throw new PaddleOcrMetadataException(
                    error,
                    "Expected model input is unavailable"
            );
        }

        if (!(inputNode.getInfo()
                instanceof TensorInfo)) {
            throw new PaddleOcrMetadataException(
                    error,
                    "Model input is not a tensor"
            );
        }

        TensorInfo inputTensor =
                (TensorInfo) inputNode.getInfo();

        if (inputTensor.type != OnnxJavaType.FLOAT) {
            throw new PaddleOcrMetadataException(
                    error,
                    "Model input must use FLOAT tensors"
            );
        }

        if (inputTensor.getShape().length
                != expectedInputRank) {
            throw new PaddleOcrMetadataException(
                    error,
                    "Model input rank is incompatible"
            );
        }

        if (session.getOutputInfo().size()
                != expectedOutputCount) {
            throw new PaddleOcrMetadataException(
                    error,
                    "Model output count is incompatible"
            );
        }
    }

    private void validateRecognizerOutputClassCount(
            OrtSession session,
            int expectedClassCount
    ) throws OrtException {
        if (session.getOutputInfo().isEmpty()) {
            throw recognizerMetadataError(
                    "Recognizer has no output"
            );
        }

        NodeInfo outputNode =
                session.getOutputInfo()
                        .values()
                        .iterator()
                        .next();

        if (!(outputNode.getInfo()
                instanceof TensorInfo)) {
            throw recognizerMetadataError(
                    "Recognizer output is not a tensor"
            );
        }

        TensorInfo outputTensor =
                (TensorInfo) outputNode.getInfo();

        if (outputTensor.type != OnnxJavaType.FLOAT) {
            throw recognizerMetadataError(
                    "Recognizer output must use FLOAT tensors"
            );
        }

        long[] shape = outputTensor.getShape();

        if (shape.length < 2) {
            throw recognizerMetadataError(
                    "Recognizer output rank is incompatible"
            );
        }

        long classDimension =
                shape[shape.length - 1];

        if (classDimension > 0
                && classDimension != expectedClassCount) {
            throw recognizerMetadataError(
                    "Recognizer class count is incompatible"
            );
        }
    }

    private PaddleOcrMetadataException
    recognizerMetadataError(String message) {
        return new PaddleOcrMetadataException(
                PaddleOcrInitializationError
                        .RECOGNIZER_METADATA_INCOMPATIBLE,
                message
        );
    }
}
